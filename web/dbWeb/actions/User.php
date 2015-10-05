<?php

require_once './DatabaseManager.php';

class User extends DatabaseManager {

    /**
     * Name of the table
     * @var type string
     */
    public static $tableName = 'user';

    /**
     * Array that holds columnds in a table.
     * Update when a database is changed
     * @var type Array
     */
    private $fields = ['firstName', 'lastName', 'email', 'password', 'amazon_turk_id'];

    public function __construct($tableName) {
        parent::__construct($tableName);
    }

    /**
     * Process of registering a user 
     * using the "insert()" function 
     * in DatabaseManager
     */
    public function register() {
        $data = [$_POST['firstName'], $_POST['lastName'], $_POST['email'], $_POST['password'], null];

        $successful = $this->insert($this->fields, $data);

        $result = true;

        if (!$successful) {
            $result = false;
        }

        echo json_encode($result);
    }

    /**
     * Process of loging in a user 
     * using the "select()" function 
     * in DatabaseManager
     */
    public function login() {
        $fields = ['email', 'password'];
        $conditions = [$_POST['email'], $_POST['password']];
        $result = $this->select($fields, $conditions);
        if (!empty($result)) {
            session_start();
            $_SESSION['userId'] = $result['0']['id'];
            $_SESSION['firstName'] = $result['0']['firstName'];
            $_SESSION['lastName'] = $result['0']['lastName'];
            $_SESSION['email'] = $result['0']['email'];
            echo json_encode(true);
            return;
        }

        echo json_encode(false);
    }

    /**
     * Process of loging out a user 
     * using the session_destroy()
     */
    public function logout() {
        session_start();
        session_destroy();
        $_SESSION = array();
        
    }

}

$user = new User(User::$tableName);
switch ($_POST['action']) {
    case 'register':
        $user->register();
        break;
    case 'login':
        $user->login();
        break;
    case 'logout':
        $user->logout();
        break;
}