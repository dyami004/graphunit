<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

class DatabaseManager {

    private $sever = "localhost";
    private $username = "root";
    private $password = "";
    private $dbName = "graphunit";
    private $mysqli;
    private $tableName;

    public function __construct($tableName) {
        $this->tableName = $tableName;
        $this->mysqli = new mysqli($this->sever, $this->username, $this->password, $this->dbName);
        if ($this->mysqli->connect_errno) {
            printf("Connect failed: %s\n", $this->mysqli->connect_error);
            exit();
        }
    }

    public function __destruct() {
        $this->mysqli->close();
    }

    /**
     * Create a new item in the database
     * 
     * @param type $fields Fields in the same order as the database
     * @param type $data Data to be inserted
     * @return type If false, insert was unsuccessful
     */
    public function insert($fields = array(), $data = array()) {
        if (empty($fields) || empty($data)) {
            die("SQL can not insert without data, fields or table.");
        }

        $columns = null;
        $columnsData = null;
        foreach ($fields as $key => $current) {
            if ($key == 0) {
                $columns = $current;
                $columnsData = $data[$key];
                continue;
            }


            if (!is_null($data[$key])) {
                $columns = $columns . "`, `" . $current;
                $columnsData = $columnsData . "', '" . $data[$key];
            }
        }

        $sqlQuery = "INSERT INTO `" . $this->tableName . "` (`" . $columns . "`) VALUES ('" . $columnsData . "')";

        return $this->mysqli->query($sqlQuery);
    }

    /**
     * Return one or multiple items from the database
     * 
     * @param type $fields Fields in same order as condition
     * @param type $conditions 0 or more conditions for the select
     * @return type array 
     */
    public function select($fields = array(), $conditions = array()) {
        $where = null;

        if (!empty($fields) || !empty($conditions)) {
            foreach ($fields as $key => $current) {
                if ($key == 0) {
                    $where = " WHERE `".$current."` = '".$conditions[$key]."'";
                    continue;
                }

                $where = $where." AND `".$current."` = '".$conditions[$key]."'";
            }
        }
        $sqlQuery = "SELECT * FROM `" . $this->tableName . "`" . $where;

        if ($result = $this->mysqli->query($sqlQuery)) {
            $arrayResult = array();
            /* fetch object array */
            while ($array = $result->fetch_array()) {
                $arrayResult[] = $array;
            }

        }
        return $arrayResult;
    }

}

?>
