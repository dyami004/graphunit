<ul class="nav nav-pills">
    <li class="active"><a  href="#studyManagementTab" data-toggle="tab">Study</a></li>
    <li><a href="#datasetManagementTab" data-toggle="tab">Dataset</a></li>
    <li><a href="#viewerManagementTab" data-toggle="tab">Viewer</a></li>
</ul>

<div class="tab-content">
    <div class="well">
        <div class="tab-pane active" id="studyManagementTab">
            <?php
            include './views/include/managementTabs/study/studyManagement.php';
            ?>
        </div>
        <div class="tab-pane" id="datasetManagementTab" >
            <?php
            include './views/include/managementTabs/dataset/datasetManagement.inc.php';
            ?>
        </div>
        <div class="tab-pane" id="viewerManagementTab" >
            <?php
            include './views/include/managementTabs/viewer/viewerManagement.inc.php';
            ?>
        </div>
    </div>
</div>

<script type="text/javascript">

</script>

