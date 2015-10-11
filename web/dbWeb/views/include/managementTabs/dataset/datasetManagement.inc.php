<?php
require './actions/Dataset.php';
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

$dataset = new Dataset(Dataset::$tableName, $userId);
$result = $dataset->datasetManagerView();
?>
<h2 class="text-center">Dataset Management  <button  class="btn btn-default text-right" data-toggle="modal" data-target="#modalViewLogin">Add Dataset</button></h2>

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
                    <button type="button" class="btn btn-default btn-xs" onclick="copy('<?php echo $value['name']; ?>','<?php echo $value['path']; ?>')">Copy</button>
                    <button type="button" class="btn btn-default btn-xs" data-toggle="modal" 
                            data-dataset-id="<?php echo $value['id']; ?>" 
                            data-dataset-name="<?php echo $value['name']; ?>" 
                            data-target="#editDataset">Edit</button>
                    <button type="button" class="btn btn-default btn-xs" onclick="del(<?php echo $value['id']; ?>)">Delete</button>
                </td>
            </tr>
            <?php
        }
        ?>
    </table>
</div>

<script type="text/javascript">
    var userId = <?php echo $userId; ?>;
    function copy(name, path) {
        $.ajax({
            type: "POST",
            url: "actions/Dataset.php",
            data: {
                action: 'copy',
                userId: userId,
                name: name,
                path: path
            },
            success: function (data)
            {
                if (data === "true") {
                    var hash = window.location.hash;
                    hash && $('ul.nav a[href="' + hash + '"]').tab('show');
                } else {
                    alert("There was an error, please try again.");
                }
            }
        });

    }
    function edit(id) {
        $.ajax({
            type: "POST",
            url: "actions/Dataset.php",
            data: {
                action: 'edit',
                userId: userId,
                id: id
            },
            success: function (data)
            {
                if (data === "true") {
                } else {
                    alert("There was an error, please try again.");
                }
            }
        });
    }
    function del(id) {
        $.ajax({
            type: "POST",
            url: "actions/Dataset.php",
            data: {
                action: 'delete',
                userId: userId,
                id: id
            },
            success: function (data)
            {
                if (data === "true") {
                    location.reload();
                    $(document).ready(function () {
                        $('.nav-tabs a[href="#datasetManagementTab"]').tab('show');
                    });
                } else {
                    alert("There was an error, please try again.");
                }
            }
        });
    }
</script>