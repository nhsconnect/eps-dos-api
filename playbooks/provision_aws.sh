#!/bin/bash

set -u # Variables must be explicit
set -e # If any command fails, fail the whole thing
set -o pipefail

# Make sure SSH knows to use the correct pem
#ssh-add myapp.pem
#ssh-add -l
# Load the AWS keys
source ./aws_keys

# set up the network
ansible-playbook $1 provision_aws_vpc.yaml
