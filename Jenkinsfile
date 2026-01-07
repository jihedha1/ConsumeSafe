// Jenkinsfile (Version finale avec Trivy pour le SCA)
pipeline {
    agent any
    tools {
        maven 'M2_HOME'
        jdk 'JDK21'
    }
    environment {
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
        DOCKER_REGISTRY_URL = 'https://registry.hub.docker.com'
        DOCKER_IMAGE_NAME = "votre-dockerhub-id/consumesafe-spring"
    }

    stages {
        // ==================================================================
        // STAGE 1: BUILD & TEST
        // ==================================================================
        stage('Build & Test' ) {
            steps {
                echo '--- Compiling, running unit tests, and packaging ---'
                // On ne fait que construire et tester ici. Pas de scan.
                sh 'mvn clean package'
            }
        }

        // ==================================================================
        // STAGE 2: SCA SCAN AVEC TRIVY
        // ==================================================================
        stage('Security Scan (SCA) with Trivy') {
            steps {
                echo '--- Scanning project dependencies with Trivy ---'
                // Trivy scanne le système de fichiers du projet.
                // Il détecte automatiquement le pom.xml et analyse les dépendances.
                // '--exit-code 1' et '--severity' font échouer le build si nécessaire.
                sh "trivy fs --exit-code 1 --severity CRITICAL,HIGH ."
            }
        }

        // ==================================================================
        // STAGE 3: BUILD DOCKER IMAGE
        // ==================================================================
        stage('Build Docker Image') {
            steps {
                echo "--- Building Docker image: ${DOCKER_IMAGE_NAME}:${env.BUILD_ID} ---"
                script {
                    def dockerImage = docker.build("${DOCKER_IMAGE_NAME}:${env.BUILD_ID}", '.')
                    docker.withRegistry(DOCKER_REGISTRY_URL, DOCKER_CREDENTIALS_ID) {
                        dockerImage.push()
                    }
                }
            }
        }

        // ==================================================================
        // STAGE 4: SCAN DOCKER IMAGE (avec Trivy aussi)
        // ==================================================================
        stage('Scan Docker Image with Trivy') {
            steps {
                echo '--- Scanning Docker image for OS-level vulnerabilities ---'
                sh "trivy image --exit-code 1 --severity CRITICAL,HIGH ${DOCKER_IMAGE_NAME}:${env.BUILD_ID}"
            }
        }

        // ==================================================================
        // STAGE 5: DEPLOY TO KUBERNETES
        // ==================================================================
        stage('Deploy to Kubernetes') {
            when { branch 'main' }
            steps {
                echo "--- Deploying version ${env.BUILD_ID} to Kubernetes ---"
                script {
                    sh "sed -i 's|image: .*|image: ${DOCKER_IMAGE_NAME}:${env.BUILD_ID}|g' kubernetes/deployment.yaml"
                    sh 'kubectl apply -f kubernetes/'
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
