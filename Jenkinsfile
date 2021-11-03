pipeline {
    agent{
        label 'jenkins-workers'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: "10"))
    }

    environment {
        ECR_REPO_DIR = "pss"
    }
    
    stages {
        stage('Stage 1') {
            steps {
                echo 'Hello world!' 
            }
        }
    }
}
