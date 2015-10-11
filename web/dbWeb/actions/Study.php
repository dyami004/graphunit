<?php

require_once '/DatabaseManager.php';

require_once '/Session.php';

class Study extends DatabaseManager {

    /**
     * Name of the table
     * @var type string
     */
    public static $tableName = 'study';

    /**
     * Array that holds columnds in a table.
     * Update when a database is changed
     * @var type Array
     */
    private $fields = ['name', 'path', 'user_id'];

    public function __construct($tableName) {
        parent::__construct($tableName);
    }
    /*
     * 
     */

    public function studyManagerView() {
        $fields = ['user_id'];
        $conditions = [];
        return $this->select($fields, $conditions);
    }

}
