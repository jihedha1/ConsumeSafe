// Jenkinsfile
pipeline {
    // 'agent any' signifie que Jenkins peut utiliser n'importe quel agent disponible pour exécuter ce pipeline.
    agent any

    // Définition des outils qui seront utilisés. Jenkins les mettra à disposition dans le PATH.
    // Les noms ('Maven-3.9.6') doivent correspondre EXACTEMENT à ceux configurés dans Jenkins > Global Tool Configuration.
    tools {
        maven 'M2_HOME'
        jdk 'JDK21' // Assurez-vous que 'JDK21' est le nom de votre configuration JDK dans Jenkins
    }

    // Définition des variables d'environnement pour tout le pipeline.
    environment {
        // L'ID de vos credentials Docker Hub dans Jenkins.
        DOCKER_CREDENTIALS_ID = 'dockerhub-credentials'
        // L'URL du registre Docker.
        DOCKER_REGISTRY_URL = 'https://registry.hub.docker.com'
        // Le nom de votre image Docker. Remplacez 'votre-dockerhub-id' par votre ID Docker Hub.
        DOCKER_IMAGE_NAME = "votre-dockerhub-id/consumesafe-spring"
    }

    stages {
        // ==================================================================
        // STAGE 1: BUILD & TEST - Compilation et tests unitaires
        // ==================================================================
        stage('Build & Test' ) {
            steps {
                echo '--- Compiling, running unit tests, and packaging ---'
                // 'mvn clean install' est la commande standard pour les projets Maven.
                // Elle compile le code, exécute les tests (JUnit) et crée le fichier .jar.
                // Si un test échoue, le pipeline s'arrêtera ici.
                sh 'mvn clean install'
            }
        }

        // ==================================================================
        // STAGE 2: SECURITY SCAN - Analyse des vulnérabilités
        // ==================================================================
        stage('Security Scan (SCA)') {
            steps {
                echo '--- Scanning for known vulnerabilities in dependencies ---'
                // Utilise le plugin OWASP Dependency-Check pour Maven.
                // Il scanne toutes les librairies (.jar) utilisées par le projet.
                // Le build échouera si une vulnérabilité critique est trouvée (configurable dans le pom.xml).
                sh 'mvn org.owasp:dependency-check-maven:check'
            }
        }

        // ==================================================================
        // STAGE 3: BUILD DOCKER IMAGE - Création de l'image conteneurisée
        // ==================================================================
        stage('Build Docker Image') {
            steps {
                echo "--- Building Docker image: ${DOCKER_IMAGE_NAME}:${env.BUILD_ID} ---"
                script {
                    // Utilise le plugin Docker Pipeline pour construire l'image.
                    // Le tag de l'image inclut le numéro de build Jenkins (${env.BUILD_ID}) pour une traçabilité parfaite.
                    def dockerImage = docker.build("${DOCKER_IMAGE_NAME}:${env.BUILD_ID}", '.')

                    // Se connecte au registre Docker en utilisant les credentials configurés.
                    docker.withRegistry(DOCKER_REGISTRY_URL, DOCKER_CREDENTIALS_ID) {
                        // Pousse l'image vers le registre (Docker Hub).
                        echo "--- Pushing Docker image to registry ---"
                        dockerImage.push()
                    }
                }
            }
        }

        // ==================================================================
        // STAGE 4: SCAN DOCKER IMAGE - Sécurité de l'image
        // ==================================================================
        stage('Scan Docker Image with Trivy') {
            steps {
                echo '--- Scanning Docker image for OS-level vulnerabilities ---'
                // Trivy scanne l'image que nous venons de pousser sur Docker Hub.
                // '--exit-code 1' fait échouer le pipeline si des vulnérabilités CRITICAL ou HIGH sont trouvées.
                // '--severity CRITICAL,HIGH' ne rapporte que les failles les plus importantes.
                sh "trivy image --exit-code 1 --severity CRITICAL,HIGH ${DOCKER_IMAGE_NAME}:${env.BUILD_ID}"
            }
        }

        // ==================================================================
        // STAGE 5: DEPLOY TO KUBERNETES - Déploiement continu
        // ==================================================================
        stage('Deploy to Kubernetes') {
            // Cette étape ne s'exécutera QUE si le build est sur la branche 'main'.
            // Cela empêche le déploiement automatique depuis les branches de développement.
            when { branch 'main' }

            steps {
                echo "--- Deploying version ${env.BUILD_ID} to Kubernetes ---"
                script {
                    // On utilise 'sh' pour exécuter des commandes shell.
                    // Cette commande met à jour le fichier de déploiement Kubernetes
                    // pour utiliser la nouvelle version de l'image que nous venons de construire.
                    sh "sed -i 's|image: .*|image: ${DOCKER_IMAGE_NAME}:${env.BUILD_ID}|g' kubernetes/deployment.yaml"

                    // Applique les configurations au cluster Kubernetes.
                    // 'kubectl' doit être disponible pour l'agent Jenkins.
                    sh 'kubectl apply -f kubernetes/'
                }
            }
        }
    }

    // ==================================================================
    // POST-BUILD ACTIONS - Actions à exécuter après la fin du pipeline
    // ==================================================================
    post {
        // 'always' s'exécute toujours, que le pipeline ait réussi ou échoué.
        always {
            echo 'Pipeline finished. Cleaning up workspace...'
            // 'cleanWs()' nettoie l'espace de travail pour le prochain build.
            cleanWs()
        }
        // On pourrait ajouter des notifications 'success', 'failure', etc.
        // par email ou sur Slack.
    }
}
