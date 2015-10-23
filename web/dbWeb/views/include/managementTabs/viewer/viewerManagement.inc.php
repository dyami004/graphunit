<?php
require './actions/Viewer.php';
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

$viewer = new Viewer(Viewer::$tableName, $userId);
$result = $viewer->viewerManagerView();
?>
<div class="container">
    <div class="row">
        <div class="col-md-offset-4">
            <div class="col-md-6">
                <h2>Viewer Management</h2>
            </div>
            <div class="col-md-3">
            </div>
            <button  class="btn btn-primary text-right" data-toggle="modal" data-target="#modalViewLogin">Add Viewer</button>
        </div>
    </div>

    <div class="container">
        <table class="table table-bordered table-hover">
            <thead>
                <tr>     
                    <th>ID</th>
                    <th>Name</th>
                    <th>Action</th>           
                </tr>
            </thead>
            <?php
            foreach ($result as $value) {
                ?>

                <tr>
                    <td>
                        <?php
                        echo $value['id'];
                        ?>
                    </td>
                    <td>
                        <?php
                        echo $value['name'];
                        ?>
                    </td>
                    <td class="text-right">
                        <button type="button" class="btn btn-default btn-xs" onclick="copy('<?php echo $value['name']; ?>', '<?php echo $value['path']; ?>')">Copy</button>
                        <button type="button" class="btn btn-default btn-xs" data-toggle="modal" 
                                data-dataset-id="<?php echo $value['id']; ?>" 
                                data-dataset-name="<?php echo $value['name']; ?>" 
                                data-target="#editDataset">Edit</button>
                        <button type="button" class="btn btn-danger btn-xs" onclick="del(<?php echo $value['id']; ?>)">Delete</button>
                    </td>
                </tr>
                <?php
            }
            ?>
        </table>
    </div>
</div>

<script type="text/javascript">
    var userId = <?php echo $userId; ?>;
    function copy(name, path) {
        $.ajax({
            type: "POST",
            url: "actions/Viewer.php",
            data: {
                action: 'copy',
                userId: userId,
                name: name,
                path: path
            },
            success: function (data)
            {
                if (data === "true") {
                    location.reload();
                } else {
                    alert("There was an error, please try again.");
                }
            }
        });

    }
//        function edit(id) {
//            $.ajax({
//                type: "POST",
//                url: "actions/Dataset.php",
//                data: {
//                    action: 'edit',
//                    userId: userId,
//                    id: id
//                },
//                success: function (data)
//                {
//                    if (data === "true") {
//                    } else {
//                        alert("There was an error, please try again.");
//                    }
//                }
//            });
//        }
    function del(id) {
        $.ajax({
            type: "POST",
            url: "actions/Viewer.php",
            data: {
                action: 'delete',
                userId: userId,
                id: id
            },
            success: function (data)
            {
                if (data === "true") {
                    location.reload();
                } else {
                    alert("There was an error, please try again.");
                }
            }
        });
    }
</script>
