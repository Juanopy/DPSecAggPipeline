kill -SIGINT $(cat /tmp/privee_pid) 2>/dev/null

if (( $? == 1 )); then
    echo "The server is not running"
    echo "check the pid the server started"
    echo "    'head /tmp/privee.log'"
    echo "and check if there was an error"
    echo "    'tail /tmp/privee.log'"
else
    echo "The Server was terminated successfully"
fi