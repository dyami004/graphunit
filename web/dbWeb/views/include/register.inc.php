
<div class="well">
    
    
    
    <form id="register" role="form" method="post" class="form-horizontal">
        <div class="form-group">
            <input name="firstName" class="form-control" id="firstName" />
        </div>
        <div class="form-group">
            <input name="lastName" class="form-control" id="lastName"/>
        </div>
        <div class="form-group">
            <input name="email" class="form-control" id="email" />
        </div>
        <div class="form-group">
            <input name="password" type="password" class="form-control" id="password" />
        </div>

        <button id="register" type="submit" class="btn btn-default">Register</button>
    </form>
</div>
<script type="text/javascript">
                console.log(location.host);
                console.log(location.hostname);
    $('#register').submit(function (e) {
        e.preventDefault();
    $.post(window.location.href = "/graphunit/web/dbWeb/actions/DatabaseManager.php",
    {
        name: "Donald Duck",
        city: "Duckburg"
    },
    function(data, status){
        alert("Data: " + data + "\nStatus: " + status);
    });
//        $.ajax({
//            type: "POST",
//            url:  window.location.href = "/graphunit/web/dbWeb/actions/DatabaseManager.php",
//            data: $("#register").serialize(), // serializes the form's elements.
//            success: function (data)
//            {
//                
//                
//            }
//        });
    });
</script>
