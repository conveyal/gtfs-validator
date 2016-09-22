{ echo "
git stash -q --keep-index
# Using "mvn test" to run all unit tests and run plugins to assert
#   * code coverage threshold >= 85% (using surefire, enforcer plugins)
#   * FindBugs at low threshold errors (using findbugs-maven-plugin)
#   * Checkstyle has 0 errors (using maven-checkstyle-plugin)
/usr/local/bin/mvn clean test
RESULTS=\$?
# Perform checks
git stash pop -q
if [ \$RESULTS -ne 0 ]; then
  echo Error: Commit criteria not met with one or more of the following issues,
  echo 1. Failure\(s\) in unit tests
  echo 2. Failure to meet 85% code coverage
  echo 3. Failure to meet low FindBugs threshold
  echo 4. Failure to meet 0 Checkstyle errors
  exit 1
fi
# You shall commit
exit 0"
} > pre-commit.sh
pushd .git/hooks
ln -sf ../../pre-commit.sh pre-commit
chmod u+x pre-commit
popd
