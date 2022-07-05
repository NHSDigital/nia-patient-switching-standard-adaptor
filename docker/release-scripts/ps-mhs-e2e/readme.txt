There are three steps to the PS Adaptor setup

1. Update the vars.sh to run as you would like in your local instance.
2. Run script 01-start-local-ps-adaptor-with-mhs

This will set up our docker environment and trigger off our migration process, you'll have to wait until
the DB migration is complete which will take a couple of minutes, and can be checked by looking at the migration container cli.
A success message should be visible once done.

3. Run script 02-snowmed-db-setup.sh

This will load all snowmed codes and immunizations.