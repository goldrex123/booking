pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                sh '''
                    chmod +x gradlew
                    ./gradlew clean test --no-daemon
                '''
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Build') {
            steps {
                sh './gradlew build -x test --no-daemon'
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([
                    file(credentialsId: 'booking-env-file', variable: 'ENV_FILE'),
                    file(credentialsId: 'booking-application-prod-yaml', variable: 'PROD_YAML_FILE')
                ]) {
                    sh '''
                        install -m 600 "$ENV_FILE" .env
                        install -m 600 "$PROD_YAML_FILE" src/main/resources/application-prod.yaml
                    '''
                }
                sh '''
                    chmod +x deploy.sh
                    ./deploy.sh
                '''
            }
            post {
                always {
                    sh 'rm -f .env src/main/resources/application-prod.yaml'
                }
            }
        }
    }
}
