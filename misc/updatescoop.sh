#!/usr/bin/env bash
########################################################################
### Update scoop

set -e

mulefd_version=`ls build/distributions/mulefd-*.*.zip | sed -e 's/.*mulefd-\(.*\).zip/\1/g'`
echo "Updating mulefd scoop with version $mulefd_version from `pwd`"
DIST=`ls build/distributions/mulefd-${mulefd_version}.zip | cut -f1 -d ' '`
sha256=`cat $DIST.sha256`

rm -rf scoop-bucket
git clone https://github.com/manikmagar/scoop-bucket.git

cp build/scoop/mulefd.json scoop-bucket/mulefd.json

cd scoop-bucket

git config user.name "Manik Magar"
git config user.email "${GIT_USER_EMAIL}"


git add mulefd.json
git commit -m "mulefd v${mulefd_version}"
git tag -a "v${mulefd_version}" -m "mulefd v${mulefd_version}"

remote_repo="https://${MULEFD_USER}:${MULEFD_TOKEN}@github.com/manikmagar/scoop-bucket.git"
echo $remote_repo

git push "${remote_repo}" --follow-tags


## to test use `scoop bucket add https://github.com/manikmagar/scoop-bucket`
# scoop install mulefd