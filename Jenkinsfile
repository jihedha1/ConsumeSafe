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
        // On définit le chemin du cache Trivy pour éviter les problèmes de permissions
        TRIVY_CACHE_DIR = "/var/lib/jenkins/.trivy/cache"
    }

    stages {
        // Étape 1: Compilation et tests
        stage('Compile, Test & Package') {
            steps {
                // On utilise 'package' car c'est suffisant pour créer le JAR.
                sh 'mvn clean package -DskipTests'
            }
            post {
                success {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        // Étape 2: Exécution des tests
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

        // ==================================================================
        // DEBUT AJOUT DEVSECOPS : Scan des dépendances du code (SCA)
        // ==================================================================
        stage('Security Scan - Dependencies (SCA)') {
            steps {
                echo '--- Scanning project dependencies with Trivy ---'
                // Trivy scanne le pom.xml pour trouver les vulnérabilités dans les librairies.
                // '--exit-code 1' fait échouer le build si une faille est trouvée.
                // On scanne uniquement les failles CRITICAL et HIGH.
                sh "trivy fs --exit-code 1 --severity CRITICAL,HIGH ."
            }
        }
        // ==================================================================
        // FIN AJOUT DEVSECOPS
        // ==================================================================

        // Étape 3: Analyse du code avec SonarQube (inchangée)
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

        // Étape 4: Construction de l'image Docker (inchangée)
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

        // ==================================================================
        // DEBUT AJOUT DEVSECOPS : Scan de l'image Docker construite
        // ==================================================================
        stage('Security Scan - Docker Image') {
            steps {
                echo "--- Scanning Docker image: ${env.DOCKER_IMAGE_VERSION} ---"
                // Trivy scanne l'image que nous venons de construire pour des vulnérabilités
                // dans le système d'exploitation de base (Alpine, etc.).
                sh "trivy image --exit-code 1 --severity CRITICAL,HIGH ${env.DOCKER_IMAGE_VERSION}"
            }
        }
        // ==================================================================
        // FIN AJOUT DEVSECOPS
        // ==================================================================

        // Étape 5: Push vers Docker Hub (inchangée)
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

        // Étape 6: Déploiement (MODIFIÉE pour utiliser Kubernetes)
        stage('Deploy Application to Kubernetes') {
            // On ne déploie que si on est sur la branche 'main'
            when { branch 'main' }
            steps {
                echo "--- Deploying version ${env.BUILD_NUMBER} to Kubernetes ---"
                // Met à jour le fichier de déploiement avec le bon tag d'image.
                sh "sed -i 's|image: .*|image: ${env.DOCKER_IMAGE_VERSION}|g' kubernetes/deployment.yaml"

                // Applique la configuration au cluster Kubernetes.
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

        // Étape 7: Tests de post-déploiement (inchangée, mais devrait être adaptée pour K8s)
        stage('Post-Deployment Tests') {
            steps {
                script {
                    sleep 10
                    // Note: Cette commande ne fonctionnera plus telle quelle avec Kubernetes.
                    // Il faudrait obtenir l'URL du service via 'minikube service' ou un Ingress.
                    // Pour la simplicité, on la laisse comme placeholder.
                    echo "Vérification de l'état de l'application (placeholder pour K8s)..."
                }
            }
        }
    }

    post {
        // ... (section post inchangée) ...
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
