<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
    <head>
        <?php
        include './actions/Session.php';
        include './views/include/head.inc.php';
        include './views/include/bootstrap.inc.php';
        ?>

    </head>
    <body>
        <div class="wrapper" style="padding-bottom: 70px;">
            <?php
            include './views/include/nav.inc.php';
            ?>
        </div>
        <div class="container">
            <div class="jumbotron">
                <?php
                include './views/include/welcome.inc.php';
                ?>
            </div>
        </div>

        <?php
        include './views/include/modalViews.inc.php';
        ?>
    </body>
</html>