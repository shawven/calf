<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
<button onclick="get()">Get</button>
<button onclick="post()">Post</button>
</body>
<script>
    function get() {
        let request = new XMLHttpRequest();
        request.open("get", "http://localhost:8888", true)
        request.withCredentials = true;
        request.send()
        request.onreadystatechange = function (state) {
            console.log(state)
        }
    }
    function post() {
        let request = new XMLHttpRequest();
        request.open("post", "http://localhost:8888", true)
        request.withCredentials = true;
        request.send()
        request.onreadystatechange = function (state) {
            console.log(state)
        }
    }
</script>
</html>
