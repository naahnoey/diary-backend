pipeline {
    agent any

    environment {
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
    }

    stages {
        stage('Pull') {
            steps {
                git branch: 'main',
                    credentialsId: 'github-ssh',
                    url: 'git@github.com:naahnoey/diary-backend.git'
            }
        }

        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean build -x test'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t diary-backend .'
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([file(credentialsId: 'app-env-file', variable: 'ENV_FILE')]) {
                    sh '''
                        docker stop diary-backend || true
                        docker rm diary-backend || true
                        docker run -d \
                            --name diary-backend \
                            -p 8080:8080 \
                            --env-file $ENV_FILE \
                            --restart always \
                            diary-backend
                    '''
                }
            }
        }
    }

    post {
        success {
            echo '배포 성공!'
        }
        failure {
            echo '배포 실패!'
        }
    }
}