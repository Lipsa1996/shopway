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
                    sh './mvnw clean verify'
                }
            }
        }
    }
}