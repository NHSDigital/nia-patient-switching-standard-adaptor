String tfProject             = "nia"
String tfPrimaryDeploymentEnv     = "kdev"
String tfSecondaryDeploymentEnv   = "kdev"
String tfComponent           = "pss"
String redirectEnv           = "kdev"         // Name of environment where TF deployment needs to be re-directed
String redirectBranch        = "main"      // When deploying branch name matches, TF deployment gets redirected to environment defined in variable "redirectEnv"
Boolean publishGPC_FacadeImage  = true // true: to publsh gpc_facade image to AWS ECR gpc_facade
Boolean publishGP2GP_TranslatorImage  = true // true: to publsh gp2gp_translator image to AWS ECR gp2gp-translator
Boolean publishMhsMockImage  = true // true: to publsh mhs mock image to AWS ECR pss-mock-mhs
Boolean secondarydeployment  = false // 


pipeline {
    agent{
        label 'jenkins-workers'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: "10"))
    }

    environment {
        BUILD_TAG = sh label: 'Generating build tag', returnStdout: true, script: 'python3 scripts/tag.py ${GIT_BRANCH} ${BUILD_NUMBER} ${GIT_COMMIT}'
        
        GPC_FACADE_ECR_REPO_DIR = "pss_gpc_facade"
        GP2GP_TRANSLATOR_ECR_REPO_DIR = "pss_gp2gp-translator"
        MHS_MOCK_ECR_REPO_DIR = "pss-mock-mhs"
        
        GPC_FACADE_DOCKER_IMAGE = "${DOCKER_REGISTRY}/${GPC_FACADE_ECR_REPO_DIR}:${BUILD_TAG}"
        GP2GP_TRANSLATOR_DOCKER_IMAGE = "${DOCKER_REGISTRY}/${GP2GP_TRANSLATOR_ECR_REPO_DIR}:${BUILD_TAG}"
        MHS_MOCK_DOCKER_IMAGE  = "${DOCKER_REGISTRY}/${MHS_MOCK_ECR_REPO_DIR}:${BUILD_TAG}"
    }
    
    stages {
        stage('Build') {
            stages {
                stage('Tests') {
                    stages {
                        stage('DB setup') {
                            steps {
                                script {
                                    sh '''
                                        source docker/vars.local.tests.sh
                                        docker network create ps-network || true
                                        docker-compose -f docker/docker-compose.yml up -d ps_db
                                        docker-compose -f docker/docker-compose.yml up db_migration
                                    '''
                                }
                            }
                        }
                        stage('GPC API Facade Tests') {
                            steps {
                                script {
                                    sh '''
                                        source docker/vars.local.tests.sh
                                        docker-compose -f docker/docker-compose.yml -f docker/docker-compose-tests.yml build gpc_facade
                                        docker-compose -f docker/docker-compose.yml -f docker/docker-compose-tests.yml up --exit-code-from gpc_facade gpc_facade activemq
                                    '''
                                }
                            }
                        }
                        stage('GP2GP Translator Tests') {
                             steps {
                                script {
                                    sh '''
                                       source docker/vars.local.tests.sh
                                       docker-compose -f docker/docker-compose.yml up --build --force-recreate --no-deps -d activemq
                                       docker-compose -f docker/docker-compose.yml -f docker/docker-compose-tests.yml build gp2gp_translator
                                       docker-compose -f docker/docker-compose.yml -f docker/docker-compose-tests.yml up --exit-code-from gp2gp_translator gp2gp_translator
                                   '''
                               }
                           }
                       }
                    }
                    post {
                        always {
                            sh '''
                               docker-compose -f docker/docker-compose.yml -f docker/docker-compose-tests.yml down --rmi all --remove-orphans
                               docker network rm ps-network
                            '''
                        }
                    }
                }
                stage('Build Docker Images') {
                    steps {
                        script {
                            if (publishGPC_FacadeImage) {
                                if (sh(label: "Running ${GPC_FACADE_ECR_REPO_DIR} docker build", script: 'docker build -f docker/gpc-facade/Dockerfile -t ${GPC_FACADE_DOCKER_IMAGE} .', returnStatus: true) != 0) {error("Failed to build ${GPC_FACADE_ECR_REPO_DIR} Docker image")}
                            }
                            if (publishGP2GP_TranslatorImage) {
                                if (sh(label: "Running ${GP2GP_TRANSLATOR_ECR_REPO_DIR} docker build", script: 'docker build -f docker/gp2gp-translator/Dockerfile -t ${GP2GP_TRANSLATOR_DOCKER_IMAGE} .', returnStatus: true) != 0) {error("Failed to build ${GP2GP_TRANSLATOR_ECR_REPO_DIR} Docker image")}
                            }
                            if (publishMhsMockImage) {
                                if (sh(label: "Running ${MHS_MOCK_ECR_REPO_DIR} docker build", script: 'docker build -f docker/mhs-adaptor-mock/Dockerfile -t ${MHS_MOCK_DOCKER_IMAGE} docker/mhs-adaptor-mock', returnStatus: true) != 0) {error("Failed to build ${MHS_MOCK_ECR_REPO_DIR} Docker image")}
                            }

                        }
                    }
                }
                
                stage('Push Image') {
                    when {
                        expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
                    }
                    steps {
                        script {
                            if (ecrLogin(TF_STATE_BUCKET_REGION) != 0 )  { error("Docker login to ECR failed") }

                            if (publishGPC_FacadeImage) {
                                if (sh (label: "Pushing GPC_Facade image", script: "docker push ${GPC_FACADE_DOCKER_IMAGE}", returnStatus: true) !=0) { error("Docker push ${GPC_FACADE_ECR_REPO_DIR} image failed") }
                            }

                            if (publishMhsMockImage) {
                                if (sh(label: "Pushing MHS Mock image", script: "docker push ${MHS_MOCK_DOCKER_IMAGE}", returnStatus: true) != 0) {error("Docker push ${MHS_MOCK_ECR_REPO_DIR} image failed") }
                            }

                            if (publishGP2GP_TranslatorImage) {
                                if (sh(label: "Pushing GP2GP_Translator image", script: "docker push ${GP2GP_TRANSLATOR_DOCKER_IMAGE}", returnStatus: true) != 0) {error("Docker push ${GP2GP_TRANSLATOR_ECR_REPO_DIR} image failed") }
                            }

                        }
                    }
                }

                 stage('Deploy') {
                    options {
                        lock("${tfProject}-${tfPrimaryDeploymentEnv}-${tfComponent}")
                    }
                    stages {

                        stage('Deploy to Kdev Environment using Terraform') {
                            when {
                              expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') && ( GIT_BRANCH == 'main' )  }
                            }
                            steps {
                                script {
                                    
                                    // Check if TF deployment environment needs to be redirected
                                    if (GIT_BRANCH == redirectBranch) { tfPrimaryDeploymentEnv = redirectEnv }
                                    
                                    String tfCodeBranch  = "develop"
                                    String tfCodeRepo    = "https://github.com/nhsconnect/integration-adaptors"
                                    String tfRegion      = "${TF_STATE_BUCKET_REGION}"
                                    List<String> tfParams = []
                                    Map<String,String> tfVariables = ["${tfComponent}_build_id": BUILD_TAG]

                                    dir ("integration-adaptors") {
                                      git (branch: tfCodeBranch, url: tfCodeRepo)
                                      dir ("terraform/aws") {
                                        if (terraformInit(TF_STATE_BUCKET, tfProject, tfPrimaryDeploymentEnv, tfComponent, tfRegion) !=0) { error("Terraform init failed")}
                                        if (terraform('plan', TF_STATE_BUCKET, tfProject, tfPrimaryDeploymentEnv, tfComponent, tfRegion, tfVariables) !=0 ) { error("Terraform Plan failed")}
                                        if (terraform('apply', TF_STATE_BUCKET, tfProject, tfPrimaryDeploymentEnv, tfComponent, tfRegion, tfVariables) !=0 ) { error("Terraform Apply failed")}
                                        if (terraformOutput(TF_STATE_BUCKET, tfProject, tfPrimaryDeploymentEnv, tfComponent, tfRegion) !=0) { error("Terraform output failed")}
                                      }
                                    }
                                }  //script
                            } // steps
                        } // Stage Deploy Primary Environment using Terraform

                        stage('Deploy to Secondary Deployment using Terraform') {
                           when {
                              expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') && (secondarydeployment == 'true') && ( GIT_BRANCH == 'main' )  }
                            }
                            steps {
                                script {
                                    
                                    
                                    String tfCodeBranch  = "develop"
                                    String tfCodeRepo    = "https://github.com/nhsconnect/integration-adaptors"
                                    String tfRegion      = "${TF_STATE_BUCKET_REGION}"
                                    List<String> tfParams = []
                                    Map<String,String> tfVariables = ["${tfComponent}_build_id": BUILD_TAG]

                                    dir ("integration-adaptors") {
                                      git (branch: tfCodeBranch, url: tfCodeRepo)
                                      dir ("terraform/aws") {
                                        if (terraformInitreconfigure(TF_STATE_BUCKET, tfProject, tfSecondaryDeploymentEnv, tfComponent, tfRegion) !=0) { error("Terraform init failed")}
                                        if (terraform('apply', TF_STATE_BUCKET, tfProject, tfSecondaryDeploymentEnv, tfComponent, tfRegion, tfVariables) !=0 ) { error("Terraform Apply failed")}
                                      }
                                    }
                              }  // script
                            } // steps
                        } // Stage Deploy Secondary Deployment using Terraform
                    }//Stages
                 }//Deploy

                stage('PSS DB Migration') {
                  when {
                    expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') && ( GIT_BRANCH == 'main' )  }
                    }
                  steps {
                      script {
                        pwd
                              sh '''
                                  
                                  sed -i 's/ = /=/' ~/.psdbsecrets.tfvars
                                  source ~/.psdbsecrets.tfvars
                                  sed -i -e 's/^/export /g' -e 's/ = /=/g' ~/.tfoutput.tfvars
                                  source ~/.tfoutput.tfvars
                                  set
                                  cd db-connector
                                  ./gradlew update
                              '''
                          } // PSS DB Migration script
                  } // steps-PSS DB Migration Code
                } // stage-PSS DB Migration Code
            } //stages
        } //Stage Build 
    } //Stages
      post {
       always {
            sh label: 'Remove exited containers', script: 'docker container prune --force'
            sh label: 'Remove images tagged with current BUILD_TAG', script: 'docker image rm -f $(docker images "*/*:*${BUILD_TAG}" -q) $(docker images "*/*/*:*${BUILD_TAG}" -q) || true'
        } // always
      } // post
} //Pipeline

String tfEnv(String tfEnvRepo="https://github.com/tfutils/tfenv.git", String tfEnvPath="~/.tfenv") {
  sh(label: "Get tfenv" ,  script: "git clone ${tfEnvRepo} ${tfEnvPath}", returnStatus: true)
  sh(label: "Install TF",  script: "${tfEnvPath}/bin/tfenv install"     , returnStatus: true)
  return "${tfEnvPath}/bin/terraform"
}

int terraformInit(String tfStateBucket, String project, String environment, String component, String region) {
  String terraformBinPath = tfEnv()
  println("Terraform Init for Environment: ${environment} Component: ${component} in region: ${region} using bucket: ${tfStateBucket}")
  String command = "${terraformBinPath} init -backend-config='bucket=${tfStateBucket}' -backend-config='region=${region}' -backend-config='key=${project}-${environment}-${component}.tfstate' -input=false -no-color"
  dir("components/${component}") {
    return( sh( label: "Terraform Init", script: command, returnStatus: true))
  } // dir
} // int TerraformInit

int terraformInitreconfigure(String tfStateBucket, String project, String environment, String component, String region) {
  String terraformBinPath = tfEnv()
  println("Terraform Init for Environment: ${environment} Component: ${component} in region: ${region} using bucket: ${tfStateBucket}")
  String command = "${terraformBinPath} init -reconfigure -backend-config='bucket=${tfStateBucket}' -backend-config='region=${region}' -backend-config='key=${project}-${environment}-${component}.tfstate' -input=false -no-color"
  dir("components/${component}") {
    return( sh( label: "Terraform Init", script: command, returnStatus: true))
  } // dir
} // int TerraformInit

int terraform(String action, String tfStateBucket, String project, String environment, String component, String region, Map<String, String> variables=[:], List<String> parameters=[]) {
    println("Running Terraform ${action} in region ${region} with: \n Project: ${project} \n Environment: ${environment} \n Component: ${component}")
    variablesMap = variables
    variablesMap.put('region',region)
    variablesMap.put('project', project)
    variablesMap.put('environment', environment)
    variablesMap.put('tf_state_bucket',tfStateBucket)
    parametersList = parameters
    parametersList.add("-no-color")

    // Get the secret variables for global
    String secretsFile = "etc/secrets.tfvars"
    writeVariablesToFile(secretsFile,getAllSecretsForEnvironment(environment,"nia",region))
    String terraformBinPath = tfEnv()
    List<String> variableFilesList = [
      "-var-file=../../etc/global.tfvars",
      "-var-file=../../etc/${region}_${environment}.tfvars",
      "-var-file=../../${secretsFile}"
    ]
    if (action == "apply"|| action == "destroy") {parametersList.add("-auto-approve")}
    List<String> variablesList=variablesMap.collect { key, value -> "-var ${key}=${value}" }
    String command = "${terraformBinPath} ${action} ${variableFilesList.join(" ")} ${parametersList.join(" ")} ${variablesList.join(" ")} "
    dir("components/${component}") {
      return sh(label:"Terraform: "+action, script: command, returnStatus: true)
    } // dir
} // int Terraform

int terraformOutput(String tfStateBucket, String project, String environment, String component, String region) {
  List<String> psDbSecretslist = getSecretsByPrefix("postgres",region)
  Map<String,Object> psDbSecretsExtracted = [:]
  Map<String,Object> psDbSecrets = [:]
    psDbSecretslist.each {
        String rawSecret = getSecretValue(it,region)
        psDbSecrets.put(it,rawSecret)
    }
    psDbSecretsExtracted.put("export PS_DB_OWNER_NAME",psDbSecrets.get('postgres-master-username'))
    psDbSecretsExtracted.put("export PS_DB_OWNER_PASSWORD",psDbSecrets.get('postgres-master-password'))
    psDbSecretsExtracted.put("export GP2GP_TRANSLATOR_USER_DB_PASSWORD",psDbSecrets.get('postgres_psdb_gp2gp_translator_user_password'))
    psDbSecretsExtracted.put("export GPC_FACADE_USER_DB_PASSWORD",psDbSecrets.get('postgres_psdb_gpc_facade_user_password'))

    writeVariablesToFile("~/.psdbsecrets.tfvars",psDbSecretsExtracted)
  
  String terraformBinPath = tfEnv()
  println("Terraform outputs for Environment: ${environment} Component: ${component} in region: ${region} using bucket: ${tfStateBucket}")
  String command = "${terraformBinPath} output > ~/.tfoutput.tfvars"
  dir("components/${component}") {
    return( sh( label: "Terraform Output", script: command, returnStatus: true))
  } // dir
} // int TerraformOutput

Map<String,String> collectTfOutputs(String component) {
  Map<String,String> returnMap = [:]
  dir("components/${component}") {
    String terraformBinPath = tfEnv()
    List<String> outputsList = sh (label: "Listing TF outputs", script: "${terraformBinPath} output", returnStdout: true).split("\n")
    outputsList.each {
      returnMap.put(it.split("=")[0].trim(),it.split("=")[1].trim())
    }
  } // dir
  return returnMap
}

int ecrLogin(String aws_region) {
    String ecrCommand = "aws ecr get-login --region ${aws_region}"
    String dockerLogin = sh (label: "Getting Docker login from ECR", script: ecrCommand, returnStdout: true).replace("-e none","") // some parameters that AWS provides and docker does not recognize
    return sh(label: "Logging in with Docker", script: dockerLogin, returnStatus: true)
}

// Retrieving Secrets from AWS Secrets
String getSecretValue(String secretName, String region) {
  String awsCommand = "aws secretsmanager get-secret-value --region ${region} --secret-id ${secretName} --query SecretString --output text"
  return sh(script: awsCommand, returnStdout: true).trim()
}

Map<String,Object> decodeSecretKeyValue(String rawSecret) {
  List<String> secretsSplit = rawSecret.replace("{","").replace("}","").split(",")
  Map<String,Object> secretsDecoded = [:]
  secretsSplit.each {
    String key = it.split(":")[0].trim().replace("\"","")
    Object value = it.split(":")[1]
    secretsDecoded.put(key,value)
  }
  return secretsDecoded
}

List<String> getSecretsByPrefix(String prefix, String region) {
  String awsCommand = "aws secretsmanager list-secrets --region ${region} --query SecretList[].Name --output text"
  List<String> awsReturnValue = sh(script: awsCommand, returnStdout: true).split()
  return awsReturnValue.findAll { it.startsWith(prefix) }
}

Map<String,Object> getAllSecretsForEnvironment(String environment, String secretsPrefix, String region) {
  List<String> globalSecrets = getSecretsByPrefix("${secretsPrefix}-global",region)
  println "global secrets:" + globalSecrets
  List<String> environmentSecrets = getSecretsByPrefix("${secretsPrefix}-${environment}",region)
  println "env secrets:" + environmentSecrets
  Map<String,Object> secretsMerged = [:]
  globalSecrets.each {
    String rawSecret = getSecretValue(it,region)
    if (it.contains("-kvp")) {
      secretsMerged << decodeSecretKeyValue(rawSecret)
    } else {
      secretsMerged.put(it.replace("${secretsPrefix}-global-",""),rawSecret)
    }
  }
  environmentSecrets.each {
    String rawSecret = getSecretValue(it,region)
    if (it.contains("-kvp")) {
      secretsMerged << decodeSecretKeyValue(rawSecret)
    } else {
      secretsMerged.put(it.replace("${secretsPrefix}-${environment}-",""),rawSecret)
    }
  }
  return secretsMerged
}

void writeVariablesToFile(String fileName, Map<String,Object> variablesMap) {
  List<String> variablesList=variablesMap.collect { key, value -> "${key} = ${value}" }
  sh (script: "touch ${fileName} && echo '\n' > ${fileName}")
  variablesList.each {
    sh (script: "echo '${it}' >> ${fileName}")
  }
}
