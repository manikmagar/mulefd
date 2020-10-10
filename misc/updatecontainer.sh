#!/usr/bin/env bash
########################################################################
### Update docker

set -e

mulefd_version=`ls build/distributions/mulefd-*.*.zip | sed -e 's/.*mulefd-\(.*\).zip/\1/g'`
echo "Updating mulefd docker with version $mulefd_version from `pwd`"
DIST=`ls build/distributions/mulefd-${mulefd_version}.zip | cut -f1 -d ' '`
sha256=`cat $DIST.sha256`
echo "DIST: ${DIST}"
echo "SHA256: ${sha256}"

rm -rf mulefd-docker
git clone https://github.com/manikmagar/mulefd-docker.git

cp build/container/Dockerfile mulefd-docker/Dockerfile
cp build/container/README.adoc mulefd-docker/README.adoc

cd mulefd-docker

git config user.name "manikmagar"
git config user.email "${GIT_USER_EMAIL}"


git add Dockerfile README.adoc
git commit -m "mulefd v${mulefd_version}"
git tag -a "${mulefd_version}" -m "mulefd v${mulefd_version}"

remote_repo="https://${MULEFD_USER}:${MULEFD_TOKEN}@github.com/manikmagar/mulefd-docker.git"
echo $remote_repo

git push "${remote_repo}" --follow-tags
