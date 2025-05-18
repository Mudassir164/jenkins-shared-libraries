// vars/scanForSecrets.groovy

def call(String gitRepoUrl, String gitBranch, String maxDepth) {
    // Run TruffleHog and save the output to a file
    def result = sh(script: """
        ~/bin/trufflehog git ${gitRepoUrl} --branch ${gitBranch} ${maxDepth} --json > secrets_scan_results.json  2>&1
    """, returnStdout: true).trim()

    sh(script: """
        jq '.' secrets_scan_results.json > secrets_scan_output.json
    """, returnStdout: true).trim()
    // Read the contents of the secrets_scan_results.json to check for secrets
    def verifiedSecret = sh(script: """
        jq '.verified_secrets | select(. != null)' secrets_scan_results.json
    """, returnStdout: true).trim()

    def unverifiedSecret = sh(script: """
        jq '.unverified_secrets | select(. != null)' secrets_scan_results.json
    """, returnStdout: true).trim()

    // Debug output
    echo "Verified secrets: ${verifiedSecret}"
    echo "Unverified secrets: ${unverifiedSecret}"

    // Check if any secrets are found
    if (verifiedSecret != "0" || unverifiedSecret != "0") {
        error 'Secrets found in the repository! Failing the build.'
    }
}
