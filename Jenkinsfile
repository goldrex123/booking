pipeline {
    agent any

    environment {
        APP_NAME = 'booking'
        DOCKER_IAMGE = 'booking-app:latest'
        DOCKER_NETWORK = 'sky_default'
    }

    stages {
        stage('Chekcout') {
            steps {
                checkout scm
            }
        }

        stage('Build Gradle') {
            steps {
                sh '''
                    chmod +x gradlew
                    ./gradlew clean build -x test --no-daemon
                '''
            }
        }
    }

}