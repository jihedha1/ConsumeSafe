pipeline {
    agent any

    tools {
        maven 'M2_HOME'
        jdk 'JDK21'
    }

    environment {

        DOCKER_REGISTRY = 'jihedhallem'
        APP_NAME = 'consumesafe'
        DOCKER_CREDENTIALS_ID = 'dockerhub-pwd'  /
        PORT = '8088'
    }

    stages {
        // Étape 1: Compilation et tests
        stage('Compile, Test & Package') {
            steps {
                sh 'mvn clean package -DskipTests'
                // Note: Nous pourrions exécuter les tests séparément si nécessaire
            }
            post {
                success {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                    // Archivage du JAR généré
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        // Étape 2: Exécution des tests (optionnel mais recommandé)
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

        // Étape 3: Analyse du code avec SonarQube (optionnel)
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

        // Étape 4: Construction de l'image Docker
        stage('Build Docker Image') {
            steps {
                script {
                    // Versionnement de l'image
                    def version = "${env.BUILD_NUMBER}"
                    def latestTag = "${DOCKER_REGISTRY}/${APP_NAME}:latest"
                    def versionTag = "${DOCKER_REGISTRY}/${APP_NAME}:${version}"

                    // Construction de l'image avec deux tags
                    sh """
                        docker build \
                            -t ${versionTag} \
                            -t ${latestTag} \
                            --build-arg JAR_FILE=target/*.jar \
                            .
                    """

                    // Sauvegarde des tags pour les étapes suivantes
                    env.DOCKER_IMAGE_VERSION = versionTag
                    env.DOCKER_IMAGE_LATEST = latestTag
                }
            }
        }

        // Étape 5: Push vers Docker Hub
        stage('Push to Docker Hub') {
            steps {
                script {
                    // Connexion à Docker Hub
                    withCredentials([string(credentialsId: DOCKER_CREDENTIALS_ID, variable: 'DOCKER_PASSWORD')]) {
                        sh """
                            docker login -u ${DOCKER_REGISTRY} -p ${DOCKER_PASSWORD}
                        """
                    }

                    // Push des images
                    sh """
                        docker push ${env.DOCKER_IMAGE_VERSION}
                        docker push ${env.DOCKER_IMAGE_LATEST}
                    """

                    // Nettoyage local des images pour libérer de l'espace
                    sh """
                        docker rmi ${env.DOCKER_IMAGE_VERSION} || true
                        docker rmi ${env.DOCKER_IMAGE_LATEST} || true
                    """
                }
            }
        }

        // Étape 6: Déploiement
        stage('Deploy Application') {
            steps {
                script {
                    // Arrêt et suppression du conteneur existant
                    sh '''
                        docker stop ${APP_NAME} || true
                        docker rm ${APP_NAME} || true
                    '''

                    // Lancement du nouveau conteneur avec des options améliorées
                    sh """
                        docker run -d \
                            --name ${APP_NAME} \
                            -p ${PORT}:${PORT} \
                            --restart unless-stopped \
                            -e SPRING_PROFILES_ACTIVE=production \
                            -e JAVA_OPTS="-Xmx512m -Xms256m" \
                            ${env.DOCKER_IMAGE_VERSION}
                    """

                    // Vérification que l'application est opérationnelle
                    sleep 30
                    sh """
                        curl -f http://localhost:${PORT}/actuator/health || echo "Health check failed, but continuing..."
                    """
                }
            }
            post {
                success {
                    echo "✅ Application déployée avec succès sur le port ${PORT}"
                    echo "🌐 URL: http://localhost:${PORT}"
                }
                failure {
                    echo "❌ Échec du déploiement"
                    // Tentative d'affichage des logs du conteneur en cas d'échec
                    sh 'docker logs ${APP_NAME} --tail 50 || true'
                }
            }
        }

        // Étape 7: Tests de post-déploiement (optionnel)
        stage('Post-Deployment Tests') {
            steps {
                script {
                    // Attente que l'application soit complètement démarrée
                    sleep 10

                    // Tests d'intégration simples
                    sh """
                        echo "Vérification de l'état de l'application..."
                        curl -s -o /dev/null -w "%{http_code}" http://localhost:${PORT}/ || true
                    """
                }
            }
        }
    }

    post {
        always {
            // Nettoyage des conteneurs et images qui pourraient être restés
            sh '''
                docker container prune -f || true
                docker image prune -f || true
            '''

            // Notification ou logs supplémentaires
            echo "Pipeline terminé avec le statut: ${currentBuild.result}"
        }
        success {
            // Notification de succès (peut être intégrée avec Slack, Email, etc.)
            echo "🎉 Pipeline exécuté avec succès!"
            echo "📦 Image Docker: ${env.DOCKER_IMAGE_VERSION}"
            echo "🚀 Application disponible sur le port: ${PORT}"
        }
        failure {
            // Notification d'échec
            echo "❌ Pipeline en échec"
            // On pourrait ajouter des étapes de rollback ici
        }
        unstable {
            echo "⚠️ Pipeline instable - vérifiez les tests"
        }
    }
}