#!/usr/bin/env bash
#/bin/bash -x

#set -e

export DEBUG=1

. aserta

export SCRATCH=`mktemp -d -t "$(basename $0).XXX"`
echo "Using $SCRATCH as scratch dir"
# deletes the temp directory
function cleanup {
  if [ -z ${KEEP_SCRATCH+x} ]; then
    rm -rf "$SCRATCH"
    echo "Deleted scratch dir $SCRATCH"
   else
    echo "Kept scratch dir: $SCRATCH"
   fi
}

# register the cleanup function to be called on the EXIT signal
trap cleanup EXIT

## define test helper, see https://github.com/lehmannro/assert.sh/issues/24
assert_statement(){
    # usage cmd exp_stout exp_stder exp_exit_code
    assert "$1" "$2"
    assert "( $1 ) 2>&1 >/dev/null" "$3"
    assert_raises "$1" "$4"
}
#assert_statement "echo foo; echo bar  >&2; exit 1" "foo" "bar" 1


assert_stderr(){
    assert "( $1 ) 2>&1 >/dev/null" "$2"
}

#assert_stderr "echo foo" "bar"

#http://stackoverflow.com/questions/3005963/how-can-i-have-a-newline-in-a-string-in-sh
export NL=$'\n'


echo Testing with `which muleflowdiagrams`
assert "muleflowdiagrams ./test-hello-app.xml -o $SCRATCH"
assert_raises "test -f $SCRATCH/mule-diagram.png" 0
