#!groovy

void setBuildStatus(String message, String state) {
  step([
    $class            : "GitHubCommitStatusSetter",
    reposSource       : [$class: "ManuallyEnteredRepositorySource", url: env.GIT_URL],
    contextSource     : [$class: "ManuallyEnteredCommitContextSource", context: "ci/jenkins/build-status"],
    errorHandlers     : [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
    statusResultSource: [$class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]]]
  ]);
}


pipeline {
  agent any
  environment {
    COMMIT_HASH = "${sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()}"
  }
  tools {
    maven 'Maven 3.8.1'
    jdk 'Java 15'
  }
  stages {
    stage('Test') {
      steps {
        setBuildStatus("Build pending", "PENDING")
        echo 'Testing..'
        script {
          sh "mvn -s /var/lib/jenkins/settings.xml test"
        }
        jacoco(
          execPattern: 'target/*.exec',
          classPattern: 'target/classes',
          sourcePattern: 'src/main/java',
          exclusionPattern: 'src/test*'
        )
      }
    }
    stage('Package') {
      steps {
        echo 'Packging jar file..'
        script {
          sh "mvn -s /var/lib/jenkins/settings.xml package -Dmaven.test.skip.exec"
        }
      }
    }
    stage('Analysis') {
      steps {
        echo 'Analyzing..'
        withSonarQubeEnv('sonarQube') {
          sh "mvn org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar"
        }
      }
    }
    stage("Quality Gate") {
      steps {
        timeout(time: 1, unit: 'HOURS') {
          waitForQualityGate abortPipeline: true
        }
      }
    }
    stage('Build') {
      steps {
        echo 'Building docker image..'
        sh "aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 038778514259.dkr.ecr.us-east-1.amazonaws.com"
        sh "docker build --tag utopia-ticket-payment:$COMMIT_HASH ."
        sh "docker tag utopia-ticket-payment:$COMMIT_HASH 038778514259.dkr.ecr.us-east-1.amazonaws.com/utopia-ticket-payment:$COMMIT_HASH"
        echo 'Pushing docker image to ECR..'
        sh "docker push 038778514259.dkr.ecr.us-east-1.amazonaws.com/utopia-ticket-payment:$COMMIT_HASH"
      }
      post {
        always {
          jiraSendBuildInfo site: 'java-feb-cram.atlassian.net'
        }
      }
    }
    stage('Deploy') {
      steps {
        echo 'Fetching cloud cloudformation template..'
        sh "touch ECS.yml"
        sh "rm ECS.yml"
        sh "wget https://raw.githubusercontent.com/Java-Feb-CRAM/cloud-formation/main/ECS.yml"
        echo 'Deploying cloudformation..'
        sh "aws cloudformation deploy --stack-name UtopiaTicketPaymentMS --template-file ./ECS.yml --parameter-overrides ApplicationName=TicketPaymentMS ECRepositoryUri=038778514259.dkr.ecr.us-east-1.amazonaws.com/utopia-ticket-payment:$COMMIT_HASH ExecutionRoleArn=arn:aws:iam::038778514259:role/ecsTaskExecutionRole TargetGroupArn=arn:aws:elasticloadbalancing:us-east-1:038778514259:targetgroup/TicketPaymentTG/a6822a34f0252b56 --role-arn arn:aws:iam::038778514259:role/CloudFormationECS --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM --region us-east-1"
      }
      post {
        always {
          jiraSendDeploymentInfo site: 'java-feb-cram.atlassian.net', environmentId: 'us-prod-1', environmentName: 'us-prod-1', environmentType: 'production'
        }
      }
    }
    stage('Cleanup') {
      steps {
        echo 'Cleaning up..'
        sh "docker system prune -f"
      }
    }
  }
  post {
    always {
      cleanWs(cleanWhenNotBuilt: false,
        deleteDirs: true,
        disableDeferredWipeout: true,
        notFailBuild: true,
        patterns: [[pattern: '.gitignore', type: 'INCLUDE'],
                   [pattern: '.propsfile', type: 'EXCLUDE']])
    }
    success {
      setBuildStatus("Build succeeded", "SUCCESS")
    }
    failure {
      setBuildStatus("Build failed", "FAILURE")
    }
  }
}
