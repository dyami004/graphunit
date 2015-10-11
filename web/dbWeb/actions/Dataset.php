<?php

require_once '/DatabaseManager.php';

require_once '/Session.php';

class Dataset extends DatabaseManager {
    
    /*
     * Current user id
     * @var type int
     */
    private $userId;

    /**
     * Name of the table
     * @var type string
     */
    public static $tableName = 'dataset';

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
     * Get all Datasets to populate list manager
     */

    public function datasetManagerView() {
        $fields = ['user_id'];
        $conditions = [$this->userId];
        return $this->select($fields, $conditions);
    }

    /*
     * Delete an existing dataset
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
     * Delete an existing dataset
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
     * Delete an existing dataset
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
    $dataset = new Dataset(Dataset::$tableName, $_POST['userId']);
    switch ($_POST['action']) {
        case 'delete':
            $dataset->del();
            break;
        case 'copy':
            $dataset->copy();
            break;
        case 'edit':
            $dataset->edit();
            break;
    }
}