<?php

require_once '/DatabaseManager.php';

require_once '/Session.php';

class Viewer extends DatabaseManager {
    
    /*
     * Current user id
     * @var type int
     */
    private $userId;

    /**
     * Name of the table
     * @var type string
     */
    public static $tableName = 'viewer';

    /**
     * Array that holds columnds in a table.
     * Update when a database is changed
     * @var type Array
     */
    private $fields = ['name', 'path', 'user_id'];

    public function __construct($tableName, $userId) {
        parent::__construct($tableName);
        $this->userId = $userId;
    }

    /*
     * Get all viewers to populate list manager
     */

    public function viewerManagerView() {
        $fields = ['user_id'];
        $conditions = [$this->userId];
        $order = ['id DESC'];
        return $this->select($fields, $conditions, $order);
    }

    /*
     * Delete an existing viewer
     */

    public function del() {
        $successful = $this->delete($_POST['id']);

        $result = true;

        if (!$successful) {
            $result = false;
        }

        echo json_encode($result);
    }

    /*
     * Edit an existing viewer
     */

    public function edit() {
        $successful = $this->delete($_POST['id']);

        $result = true;

        if (!$successful) {
            $result = false;
        }

        echo json_encode($result);
    }

    /*
     * Copy an existing viewer
     */

    public function copy() {
        $data = [$_POST['name'], $_POST['path'], $this->userId];
        $successful = $this->insert($this->fields, $data);

        $result = true;

        if (!$successful) {
            $result = false;
        }

        echo json_encode($result);
    }

}

if(isset($_POST['action'])) {
    $viewer = new Viewer(Viewer::$tableName, $_POST['userId']);
    switch ($_POST['action']) {
        case 'delete':
            $viewer->del();
            break;
        case 'copy':
            $viewer->copy();
            break;
        case 'edit':
            $viewer->edit();
            break;
    }
}