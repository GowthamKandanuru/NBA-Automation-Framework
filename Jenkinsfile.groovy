// =====================================================================
// NBA Test Automation - Jenkins Declarative Pipeline
// Repo  : https://github.com/GowthamKandanuru/NBA-Automation-Framework
// Modules: automation-framework | core-product-tests |
//          derived-product1-tests | derived-product2-tests
// =====================================================================

pipeline {

    agent any

    // ── Build Parameters ───────────────────────────────────────────────
    parameters {

        choice(
            name: 'MODULE',
            choices: [
                'all',
                'core-product-tests',
                'derived-product1-tests',
                'derived-product2-tests'
            ],
            description: 'Maven module to execute. "all" runs every module.'
        )

        choice(
            name: 'SUITE',
            choices: [
                'src/test/resources/CoreProductTestNg.xml',
                'src/test/resources/DerivedProduct_1TestNg.xml',
                'rc/test/resources/DerivedProduct_2TestNg.xml'
            ],
            description: 'TestNG suite file . Ignored when MODULE=all.'
        )

        choice(
            name: 'BROWSER',
            choices: ['chrome', 'firefox', 'edge'],
            description: 'Browser for local execution. Ignored when REMOTE=true.'
        )

        booleanParam(
            name: 'HEADLESS',
            defaultValue: true,
            description: 'Run browser in headless mode (recommended for CI).'
        )

        booleanParam(
            name: 'REMOTE',
            defaultValue: false,
            description: 'Run tests on Selenoid remote grid instead of local browser.'
        )

        string(
            name: 'HUB_URL',
            defaultValue: 'http://localhost:4444/wd/hub',
            description: 'Selenoid hub URL. Used only when REMOTE=true.'
        )
    }

    // ── Environment Variables ──────────────────────────────────────────
    environment {
        JAVA_HOME  = tool name: 'JDK-11',    type: 'jdk'
        MAVEN_HOME = tool name: 'Maven-3.9', type: 'maven'
        PATH       = "${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${env.PATH}"
    }

    // ── Pipeline Options ───────────────────────────────────────────────
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    // ══════════════════════════════════════════════════════════════════
    //  STAGES
    // ══════════════════════════════════════════════════════════════════
    stages {

        // ── Stage 1: Checkout ─────────────────────────────────────────
        stage('Checkout') {
            steps {
                git branch: 'master',
                    url: 'https://github.com/GowthamKandanuru/NBA-Automation-Framework.git'
                echo "✅ Checked out branch: ${env.GIT_BRANCH}"
            }
        }

        // ── Stage 2: Install Framework ────────────────────────────────
        // Compiles automation-framework and installs its JAR into the
        // local .m2 repository so all test modules can resolve it.
        stage('Install Framework') {
            steps {
                bat 'mvn clean install -pl automation-framework -am -DskipTests -q'
                echo "✅ automation-framework JAR installed to local .m2"
            }
        }

        // ── Stage 3: Run Tests ────────────────────────────────────────
        stage('Run Tests') {
            steps {
                script {

                    // ── Resolve module argument ───────────────────────
                    def moduleArg = (params.MODULE == 'all')
                        ? ''
                        : "-pl ${params.MODULE} -am"

                    // ── Resolve suite argument ────────────────────────
                    // Only pass -DsuiteFile when a specific module is chosen.
                    // When MODULE=all, each module uses its own default suiteFile
                    // defined in its pom.xml <properties>.
                    def suiteArg = (params.MODULE == 'all')
                        ? ''
                        : "-DsuiteFile=${params.SUITE}"

                    // ── Resolve remote argument ───────────────────────
                    def remoteArg = params.REMOTE
                        ? "-Dremote=true -Dhub_url=${params.HUB_URL}"
                        : "-Dremote=false"

                    echo """
                    
                     Test Execution Configuration
                     MODULE  : ${params.MODULE}
                     SUITE   : ${params.SUITE}
                     BROWSER : ${params.BROWSER}
                     HEADLESS: ${params.HEADLESS}
                     REMOTE  : ${params.REMOTE}
                     HUB_URL : ${params.HUB_URL}
                    
                    """

                    bat """
                        mvn test ${moduleArg} ^
                            ${suiteArg} ^
                            -Dbrowser=${params.BROWSER} ^
                            -Dheadless=${params.HEADLESS} ^
                            ${remoteArg} ^
                            -Dallure.results.directory=target/allure-results ^
                            -Dmaven.test.failure.ignore=true
                    """
                }
            }
        }

        // ── Stage 4: Publish Allure Report ────────────────────────────
        stage('Allure Report') {
            steps {
                allure([
                    reportBuildPolicy : 'ALWAYS',
                    includeProperties : false,
                    results           : [[path: '**/target/allure-results']]
                ])
                echo "✅ Allure report published"
            }
        }

        // ── Stage 5: Archive Artifacts ────────────────────────────────
        stage('Archive Artifacts') {
            steps {
                // JUnit XML results — shows pass/fail trend in Jenkins
                junit allowEmptyResults: true,
                      testResults: '**/target/surefire-reports/*.xml'

                // Test output files (CSV, TXT exports from TC1 / TC4)
                archiveArtifacts(
                    artifacts      : '**/target/test-outputs/**/*',
                    allowEmptyArchive: true
                )

                // Log files
                archiveArtifacts(
                    artifacts      : '**/target/logs/**/*',
                    allowEmptyArchive: true
                )

                echo "✅ Artifacts archived"
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  POST ACTIONS
    // ══════════════════════════════════════════════════════════════════
    post {

        success {
            echo "✅ BUILD PASSED — All tests completed successfully."
        }

        unstable {
            echo "⚠️  BUILD UNSTABLE — Some tests failed. Check Allure report for details."
        }

        failure {
            echo "❌ BUILD FAILED — Pipeline error. Check console output."
        }

        always {
            echo "📋 Build #${env.BUILD_NUMBER} finished with status: ${currentBuild.currentResult}"
            cleanWs()
        }
    }
}
