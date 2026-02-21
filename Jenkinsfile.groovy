// =====================================================================
// Veeva Test Automation - Jenkins Pipeline
// Supports: Chrome/Firefox | Headless mode | All 4 modules
// =====================================================================
pipeline {

    agent any

    parameters {
        choice(name: 'BROWSER',  choices: ['chrome', 'firefox', 'edge'], description: 'Browser to run tests on')
        choice(name: 'MODULE',   choices: ['all', 'core-product-tests', 'derived-product1-tests', 'derived-product2-tests'], description: 'Module to run')
        booleanParam(name: 'HEADLESS', defaultValue: true, description: 'Run in headless mode')
    }

    environment {
        BROWSER  = "${params.BROWSER}"
        HEADLESS = "${params.HEADLESS}"
        ALLURE_RESULTS = 'target/allure-results'
        JAVA_HOME = tool name: 'JDK-11', type: 'jdk'
        MAVEN_HOME = tool name: 'Maven-3.9', type: 'maven'
        PATH = "${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${PATH}"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/<your-repo>/veeva-automation.git'
                echo "Checked out repo on branch: ${env.GIT_BRANCH}"
            }
        }

        stage('Build & Compile') {
            steps {
                sh 'mvn clean compile -pl automation-framework -am -q'
                echo "Compilation successful"
            }
        }

        stage('Run Tests') {
            steps {
                script {
                    def moduleArg = params.MODULE == 'all' ? '' : "-pl ${params.MODULE} -am"
                    sh """
                        mvn test ${moduleArg} \\
                            -Dbrowser=${params.BROWSER} \\
                            -Dheadless=${params.HEADLESS} \\
                            -Dmaven.test.failure.ignore=true
                    """
                }
            }
        }

        stage('Generate Allure Report') {
            steps {
                sh 'mvn allure:report -pl core-product-tests || true'
                allure([
                    includeProperties: false,
                    jdk: '',
                    properties: [],
                    reportBuildPolicy: 'ALWAYS',
                    results: [[path: '**/target/allure-results']]
                ])
                echo "Allure report generated"
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: '**/target/test-outputs/**/*', allowEmptyArchive: true
                archiveArtifacts artifacts: '**/target/logs/**/*', allowEmptyArchive: true
                junit '**/target/surefire-reports/*.xml'
                echo "Artifacts archived"
            }
        }
    }

    post {
        success {
            echo "✅ All tests completed. Allure report ready."
        }
        failure {
            echo "❌ Tests failed. Check Allure report for screenshots and logs."
        }
        always {
            cleanWs()
        }
    }
}
