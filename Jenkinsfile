pipeline {
    agent any

    tools {
        maven 'M2_HOME'
        jdk 'JDK21'
    }

    environment {
        DOCKER_REGISTRY = 'jihedhallem'
        APP_NAME = 'consumesafe'
        DOCKER_CREDENTIALS_ID = 'dockerhub-pwd'
        PORT = '8088'
        TRIVY_CACHE_DIR = "/var/lib/jenkins/.trivy/cache"
    }

    stages {
        stage('Compile, Test & Package') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
            post {
                success {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
        stage('Run Tests') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }
        stage('Security Scan - Dependencies (SCA)') {
            steps {
                echo '--- Scanning project dependencies with Trivy ---'
                // '--exit-code 1' fait échouer le build si une faille est trouvée.
                sh "trivy fs --severity CRITICAL,HIGH ."
            }
        }
        stage('Code Quality Analysis') {
            when {
                expression { env.SONAR_ENABLED == 'true' }
            }
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    def version = "${env.BUILD_NUMBER}"
                    def latestTag = "${DOCKER_REGISTRY}/${APP_NAME}:latest"
                    def versionTag = "${DOCKER_REGISTRY}/${APP_NAME}:${version}"

                    sh """
                        docker build \
                            -t ${versionTag} \
                            -t ${latestTag} \
                            --build-arg JAR_FILE=target/*.jar \
                            .
                    """

                    env.DOCKER_IMAGE_VERSION = versionTag
                    env.DOCKER_IMAGE_LATEST = latestTag
                }
            }
        }
        stage('Security Scan - Docker Image') {
            steps {
                echo "--- Scanning Docker image: ${env.DOCKER_IMAGE_VERSION} ---"
                sh "trivy image --severity CRITICAL,HIGH ${env.DOCKER_IMAGE_VERSION}"
            }
        }
        stage('Push to Docker Hub') {
            steps {
                script {
                    withCredentials([string(credentialsId: DOCKER_CREDENTIALS_ID, variable: 'DOCKER_PASSWORD')]) {
                        sh "docker login -u ${DOCKER_REGISTRY} -p ${DOCKER_PASSWORD}"
                    }
                    sh "docker push ${env.DOCKER_IMAGE_VERSION}"
                    sh "docker push ${env.DOCKER_IMAGE_LATEST}"
                    sh "docker rmi ${env.DOCKER_IMAGE_VERSION} || true"
                    sh "docker rmi ${env.DOCKER_IMAGE_LATEST} || true"
                }
            }
        }
        stage('Deploy Application to Kubernetes') {
            when { branch 'main' }
            steps {
                echo "--- Deploying version ${env.BUILD_NUMBER} to Kubernetes ---"
                sh "sed -i 's|image: .*|image: ${env.DOCKER_IMAGE_VERSION}|g' kubernetes/deployment.yaml"
                sh 'kubectl apply -f kubernetes/'
            }
            post {
                success {
                    echo "✅ Application déployée/mise à jour sur Kubernetes."
                }
                failure {
                    echo "❌ Échec du déploiement sur Kubernetes."
                }
            }
        }
        stage('Post-Deployment Tests') {
            steps {
                script {
                    sleep 10
                    echo "Vérification de l'état de l'application (placeholder pour K8s)..."
                }
            }
        }
    }

    post {
        always {
            sh '''
                docker container prune -f || true
                docker image prune -f || true
            '''
            echo "Pipeline terminé avec le statut: ${currentBuild.result}"
        }
        success {
            echo "🎉 Pipeline exécuté avec succès!"
            echo "📦 Image Docker: ${env.DOCKER_IMAGE_VERSION}"
        }
        failure {
            echo "❌ Pipeline en échec"
        }
        unstable {
            echo "⚠️ Pipeline instable - vérifiez les tests"
        }
    }
}
