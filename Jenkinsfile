String tfProject      = "nia"
String tfEnvironment  = "build1"
String tfComponent    = "pss"
String redirectEnv    = "build1"          // Name of environment where TF deployment needs to be re-directed
String redirectBranch = "main"      // When deploying branch name matches, TF deployment gets redirected to environment defined in variable "redirectEnv"
Boolean publishGPC_FacadeImage  = true // true: to publsh gpc_facade image to AWS ECR gpc_facade
Boolean publishGP2GP_TranslatorImage  = true // true: to publsh gp2gp_translator image to AWS ECR gp2gp-translator
Boolean publishMhsMockImage  = true // true: to publsh mhs mock image to AWS ECR pss-mock-mhs


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
        
        GPC_FACADE_ECR_REPO_DIR = "gpc_facade"
        GP2GP_TRANSLATOR_ECR_REPO_DIR = "gp2gp-translator"
        MHS_MOCK_ECR_REPO_DIR = "pss-mock-mhs"
        
        GPC_FACADE_DOCKER_IMAGE = "${DOCKER_REGISTRY}/${GPC_FACADE_ECR_REPO_DIR}:${BUILD_TAG}"
        GP2GP_TRANSLATOR_DOCKER_IMAGE = "${DOCKER_REGISTRY}/${GP2GP_TRANSLATOR_ECR_REPO_DIR}:${BUILD_TAG}"
        MHS_MOCK_DOCKER_IMAGE  = "${DOCKER_REGISTRY}/${MHS_MOCK_ECR_REPO_DIR}:${BUILD_TAG}"
    }
    
    stages {
        stage('Build') {
            stages {
                stage('Tests') {
                    steps {
                        script {
                            sh '''
                                source docker/vars.sh
                                docker network create pss-network || true
                                docker-compose -f docker/docker-compose.yml -f docker/docker-compose-tests.yml stop
                                docker-compose -f docker/docker-compose.yml -f docker/docker-compose-tests.yml rm -f
                                docker-compose -f docker/docker-compose.yml -f docker/docker-compose-tests.yml build
                                docker-compose -f docker/docker-compose.yml -f docker/docker-compose-tests.yml up --exit-code-from pss
                            '''
                        }
                    }
                    post {
                        always {
                            sh "docker cp tests:/home/gradle/service/build ."
                            archiveArtifacts artifacts: 'build/reports/**/*.*', fingerprint: true
                            junit '**/build/test-results/**/*.xml'
                            recordIssues(
                                enabledForFailure: true,
                                tools: [
                                    checkStyle(pattern: 'build/reports/checkstyle/*.xml'),
                                    spotBugs(pattern: 'build/reports/spotbugs/*.xml')
                                ]
                            )
                            step([
                                $class : 'JacocoPublisher',
                                execPattern : '**/build/jacoco/*.exec',
                                classPattern : '**/build/classes/java',
                                sourcePattern : 'src/main/java',
                                exclusionPattern : '**/*Test.class'
                            ])
                            sh "rm -rf build"
                            sh "docker-compose -f docker/docker-compose.yml -f docker/docker-compose-tests.yml down"
                            sh "docker network rm pss-network"
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
                                if (sh(label: "Running ${MHS_MOCK_ECR_REPO_DIR} docker build", script: 'docker build -f docker/mhs-adaptor-mock/Dockerfile -t ${MHS_MOCK_DOCKER_IMAGE} .', returnStatus: true) != 0) {error("Failed to build ${MHS_MOCK_ECR_REPO_DIR} Docker image")}
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
                            if (ecrLogin(TF_STATE_BUCKET_REGION) != 0 )  { error("Docker login to ECR failed")

                            if (publishWiremockImage) {
                                if (sh (label: "Pushing Wiremock image", script: "docker push ${GPC_FACADE_DOCKER_IMAGE}", returnStatus: true) !=0) { error("Docker push ${GPC_FACADE_ECR_REPO_DIR} image failed") }
                            }

                            if (publishMhsMockImage) {
                                if (sh(label: "Pushing MHS Mock image", script: "docker push ${MHS_MOCK_DOCKER_IMAGE}", returnStatus: true) != 0) {error("Docker push ${MHS_MOCK_ECR_REPO_DIR} image failed") }
                            }

                            if (publishGpccMockImage) {
                                if (sh(label: "Pushing GPCC Mock image", script: "docker push ${GP2GP_TRANSLATOR_DOCKER_IMAGE}", returnStatus: true) != 0) {error("Docker push ${GP2GP_TRANSLATOR_ECR_REPO_DIR} image failed") }
                            }

                        }
                    }
                }
             } //Stages Build
          } //Stage Build 
       } //Stages
    } // Pipeline
    
int ecrLogin(String aws_region) {
    String ecrCommand = "aws ecr get-login --region ${aws_region}"
    String dockerLogin = sh (label: "Getting Docker login from ECR", script: ecrCommand, returnStdout: true).replace("-e none","") // some parameters that AWS provides and docker does not recognize
    return sh(label: "Logging in with Docker", script: dockerLogin, returnStatus: true)
}
