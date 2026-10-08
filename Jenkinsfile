pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'shopway-jwt-secret',
                        variable: 'JWT_SECRET'
                    )
                ]) {
                    withEnv(["JAVA_HOME=${tool 'Java-25'}"]) {
                        sh '''
                            java -version
                            ./mvnw clean verify
                        '''
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withEnv(["JAVA_HOME=${tool 'Java-25'}"]) {
                    withSonarQubeEnv('SonarQube') {
                        sh './mvnw sonar:sonar -Dsonar.projectKey=Shopway'
                    }
                }
            }
        }
    }
}