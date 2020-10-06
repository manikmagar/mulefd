#!/usr/bin/env bash
########################################################################
### Update homebrew

set -e

mulefd_version=`ls build/distributions/mulefd-*.*.zip | sed -e 's/.*mulefd-\(.*\).zip/\1/g'`
echo "Updating mulefd brew with version $mulefd_version from `pwd`"
DIST=`ls build/distributions/mulefd-${mulefd_version}.zip | cut -f1 -d ' '`
sha256=`cat $DIST.sha256`

rm -rf homebrew-tap
git clone https://github.com/manikmagar/homebrew-tap.git

cp build/brew/formula/mulefd.rb homebrew-tap/Formula/mulefd.rb

cd homebrew-tap

git config user.name "Manik Magar"
git config user.email "${GIT_USER_EMAIL}"


git add Formula/mulefd.rb
git commit -m "mulefd v${mulefd_version}"
git tag -a "v${mulefd_version}" -m "mulefd v${mulefd_version}"

remote_repo="https://${MULEFD_USER}:${MULEFD_TOKEN}@github.com/manikmagar/homebrew-tap.git"
echo $remote_repo

git push "${remote_repo}" --follow-tags

## to test use `brew install manikmagar/tap/mulefd`