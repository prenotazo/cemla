<?PHP
/*
    Registration/Login script from HTML Form Guide
    V1.0

    This program is free software published under the
    terms of the GNU Lesser General Public License.
    http://www.gnu.org/copyleft/lesser.html
    

This program is distributed in the hope that it will
be useful - WITHOUT ANY WARRANTY; without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE.

For updates, please visit:
http://www.html-form-guide.com/php-form/php-registration-form.html
http://www.html-form-guide.com/php-form/php-login-form.html
*/
require_once("./../../library/PHPMailer_v5.1/class.phpmailer.php");
require_once("./../../library/formvalidator.php");
require_once("./../../include/ColoredDiff.php");

class FGMembersite {
    var $admin_email;
    var $from_address;
    
    var $username;
    var $pwd;
    var $database;
    var $tablename;
    var $connection;
    var $rand_key;
    
    var $error_message;
    
    //-----Initialization -------
    function FGMembersite() {
        $this->sitename = 'ricardocastiglione.com.ar';
        $this->rand_key = '0iQx5oBk66oVZep';
    }
    
    function InitDB($host,$uname,$pwd,$database,$tablename) {
        $this->db_host  = $host;
        $this->username = $uname;
        $this->pwd  = $pwd;
        $this->database  = $database;
        $this->tablename = $tablename;
        
    }
    
    function SetAdminEmail($email) {
        $this->admin_email = $email;
    }
    
    function SetWebsiteName($sitename) {
        $this->sitename = $sitename;
    }
    
    function SetRandomKey($key) {
        $this->rand_key = $key;
    }
    
    //-------Main Operations ----------------------
    function getDiffWithPrevious($changeDetectedId) {
    	if(!$this->checkLogin()) {
    		$this->HandleError("Not logged in!");
    		return false;
    	}
    	 
    	if(!$this->DBLogin()) {
    		$this->HandleError("Database login failed!");
    		return false;
    	}
    	 
    	$qry = "SELECT CD.* FROM PM_CHANGEDETECTED CD WHERE CD.id = ".$changeDetectedId."";
    	$result = mysql_query($qry, $this->connection);
    	if((!$result) || (mysql_num_rows($result) == 0)) {
    		return null;
    	} 
    	$row = mysql_fetch_assoc($result);

    	$qryPrevious = "SELECT CD.* FROM PM_CHANGEDETECTED CD WHERE CD.id < ".$changeDetectedId." ORDER BY CD.id DESC LIMIT 1";
    	$resultPrevious = mysql_query($qryPrevious, $this->connection);
    	if((!$resultPrevious) || (mysql_num_rows($resultPrevious) == 0)) {
    		return $row['html'];
    	}
    	$rowPrevious = mysql_fetch_assoc($resultPrevious);
    	
    	$html = $row['html'];
    	$htmlPrevious = $rowPrevious['html'];
    	
    	$diff = new ColoredDiff($htmlPrevious, $html);
    	return $diff->render();
    }
    
    function getChangeDetected($changeDetectedId) {
    	if(!$this->checkLogin()) {
    		$this->HandleError("Not logged in!");
    		return false;
    	}
    	 
    	if(!$this->DBLogin()) {
    		$this->HandleError("Database login failed!");
    		return false;
    	}
    	 
    	$qry = "SELECT CD.* FROM PM_CHANGEDETECTED CD WHERE CD.id = ".$changeDetectedId."";
    	$result = mysql_query($qry, $this->connection);
    	if((!$result) || (mysql_num_rows($result) == 0)) {
    		return null;
    	}
    	 
    	$row = mysql_fetch_assoc($result);
    	return $row;
    }
    
    function getChangesDetected() {
    	if(!$this->checkLogin()) {
    		$this->HandleError("Not logged in!");
    		return false;
    	}
    	
    	if(!$this->DBLogin()) {
    		$this->HandleError("Database login failed!");
    		return false;
    	}
    	
    	$email = $this->getUserEmail();
    	
    	$pageMonitoredId = $this->getPageMonitoredId($email);
    	
    	$qry = "SELECT CD.* FROM PM_CHANGEDETECTED CD WHERE CD.pageMonitoredId = ".$pageMonitoredId." ORDER BY dateChangeDetected DESC, id DESC";
    	$result = mysql_query($qry, $this->connection);
    	if((!$result) || (mysql_num_rows($result) == 0)) {
    		return null;
    	}
    	
    	$rows = array();
    	while ($row = mysql_fetch_assoc($result)) {
    		$rows[$row['id']] = $row;
    	}
    	
    	return $rows;    	
    }
    
    function registerPageMonitored() {
    	if(!$this->checkLogin()) {
    		$this->HandleError("Not logged in!");
    		return false;
    	}
    	
    	$email = $this->getUserEmail();
    	$formvars = $this->collectPageMonitoredSubmission();
    	
    	if (!$this->updatePageMonitoredToDatabase($email, $formvars)) {
    		$this->HandleError("Problem registering page monitored on the database!");
    		return false;
    	}
    	
    	$this->checkChanges();

    	return true;
    }
    
    function checkChanges() {
    	if(!$this->checkLogin()) {
    		$this->HandleError("Not logged in!");
    		return false;
    	}

    	$email = $this->getUserEmail();
    	
    	if (!$this->updateLastCheckDone($email)) {    		
    		$this->HandleError("Problem updating las check done on the database!");
    		return false;
    	}
    	
    	$pageMonitored = $this->getPageMonitored($email);
    	$url = $pageMonitored['url'];
    	$htmlCurrent = preg_replace( '/\s+/', ' ', strip_tags($this->getHtml($url)));
    	
    	$latestChangeDetected = $this->getLatestChangeDetected($email);
    	$htmlPrevious = preg_replace( '/\s+/', ' ', strip_tags($latestChangeDetected['html']));
    	
    	if (strcmp($htmlPrevious, $htmlCurrent) == 0) {
    		return true;
    	}
    	
    	if (!$this->registerChangeDetectedToDatabase($email, $htmlCurrent)) {
    		$this->HandleError("Problem registering change detected on the database!");
    		return false;
    	}
    	
    	if (!$this->updateLastChangeDetected($email)) {    		
    		$this->HandleError("Problem updating las check detected on the database!");
    		return false;
    	}
    	
    	return true;
    }
    
    function getLatestChangeDetected($email) {
    	if(!$this->checkLogin()) {
    		$this->HandleError("Not logged in!");
    		return false;
    	}
    	 
    	if(!$this->DBLogin()) {
    		$this->HandleError("Database login failed!");
    		return false;
    	}
    	 
    	$qry = "SELECT CD.* 
				FROM PM_CHANGEDETECTED CD
					INNER JOIN PM_USER U ON (U.email = '".$email."')
					INNER JOIN PM_PAGEMONITORED PM ON (PM.userId = U.id)
				WHERE CD.pageMonitoredId = PM.id
				ORDER BY CD.dateChangeDetected DESC, CD.id DESC
				LIMIT 1";
    	$result = mysql_query($qry, $this->connection);
    	if((!$result) || (mysql_num_rows($result) == 0)) {
    		return null;
    	}
    	 
    	$row = mysql_fetch_assoc($result);
    	return $row;
    }
    
    function getHtml($url) {
    	$urlSource = shell_exec('wget -O - "'.$url.'"');
//     	$urlSource = shell_exec('wget -q -U "Mozilla/5.001" -O - "'.$url.'"');
		return $urlSource;    	
    }
    
    function updateLastCheckDone($email) {
    	$pageMonitoredId = $this->getPageMonitoredId($email);
    	if (empty($pageMonitoredId)) {
    		return false;
    	}
    	
    	// update
    	$update_query = 'UPDATE PM_PAGEMONITORED SET LASTCHECKDONE = now() WHERE ID = "'.$pageMonitoredId.'"';
    	if(!mysql_query($update_query ,$this->connection)) {
    		$this->HandleDBError("Error updating data to the table\nquery:$update_query");
    		return false;
    	}
    	
    	return true;
    }

    function updateLastChangeDetected($email) {
    	$pageMonitoredId = $this->getPageMonitoredId($email);
    	if (empty($pageMonitoredId)) {
    		return false;
    	}
    	
    	// update
    	$update_query = 'UPDATE PM_PAGEMONITORED SET LASTCHANGEDETECTED = now() WHERE ID = "'.$pageMonitoredId.'"';
    	if(!mysql_query($update_query ,$this->connection)) {
    		$this->HandleDBError("Error updating data to the table\nquery:$update_query");
    		return false;
    	}
    	
    	return true;
    }
    
    function registerChangeDetectedToDatabase($email, $html) {
    	if(!$this->DBLogin()) {
    		$this->HandleError("Database login failed!");
    		return false;
    	}
    
    	$pageMonitoredId = $this->getPageMonitoredId($email);
    	if (empty($pageMonitoredId)) {
    		return false;
    	}
    	
    	// insert
    	$insert_query = 'INSERT INTO PM_CHANGEDETECTED (PAGEMONITOREDID,
	               									    DATECHANGEDETECTED,
	               									    HTML)
	               		 VALUES ("'.$pageMonitoredId.'",
                				 now(),
	               		 		 "'.mysql_real_escape_string($html, $this->connection).'")';
    
    	if(!mysql_query($insert_query, $this->connection)) {
    		$this->HandleDBError("Error inserting data to the table\nquery:$insert_query");
    		return false;
    	}
    	 
    	return true;
    }
    
    function updatePageMonitoredToDatabase($email, &$formvars) {
    	if(!$this->DBLogin()) {
    		$this->HandleError("Database login failed!");
    		return false;
    	}

    	if(!$this->IsPageRegisteredFieldUnique($formvars['monitoredUrl'],'url')) {
    		$this->HandleError("This url is already registered");
    		return false;
    	}

    	if(!$this->updatePageMonitoredIntoDB($email, $formvars)) {
    		$this->HandleError("Inserting to Database failed!");
    		return false;
    	}
    	
    	return true;
    }
    
    function updatePageMonitoredIntoDB($email, &$formvars) {
    	$pageMonitoredId = $this->getPageMonitoredId($email);
    	if ($pageMonitoredId == null) {
    		// insert
	    	$insert_query = 'INSERT INTO PM_PAGEMONITORED (USERID,
	                									   URL,
	                									   LASTCHANGEDETECTED,
	                									   LASTCHECKDONE,
	                									   CHECKFREQUENCYMIN)
	                		 VALUES ("'.$this->getUserId($email).'",
	                				 "'.$this->SanitizeForSQL($formvars['monitoredUrl']).'",
	                				 now(),
	                				 now(),
	                				 "'.$this->SanitizeForSQL($formvars['checkFrequencyMin']).'")';
	    	
	    	if(!mysql_query($insert_query ,$this->connection)) {
	    		$this->HandleDBError("Error inserting data to the table\nquery:$insert_query");
	    		return false;
	    	}
    	} else {
    		// update
    		$update_query = 'UPDATE PM_PAGEMONITORED SET URL = "'.$this->SanitizeForSQL($formvars['monitoredUrl']).'",
    													 CHECKFREQUENCYMIN = "'.$this->SanitizeForSQL($formvars['checkFrequencyMin']).'"
	                		 WHERE ID = "'.$pageMonitoredId.'"';
    		
    		if(!mysql_query($update_query ,$this->connection)) {
    			$this->HandleDBError("Error updating data to the table\nquery:$update_query");
    			return false;
    		}
    	}
    	
    	return true;
    }

    function getUserId($email) {
    	$qry = "SELECT U.ID ID FROM PM_USER U WHERE U.EMAIL = '".$email."'";
    	$result = mysql_query($qry, $this->connection);
    	if((!$result) || (mysql_num_rows($result) != 1)) {
    		return null;
    	}
    	$row = mysql_fetch_assoc($result);
    	return $row['ID'];
    }
    
    function getPageMonitoredId($email) {
    	if(!$this->DBLogin()) {
    		$this->HandleError("Database login failed!");
    		return false;
    	}
    	
    	$qry = "SELECT PM.ID ID
    			FROM PM_PAGEMONITORED PM
    				INNER JOIN PM_USER U ON (U.EMAIL = '".$email."') 
    			WHERE PM.USERID = U.ID";
    	$result = mysql_query($qry, $this->connection);
    	if((!$result) || (mysql_num_rows($result) != 1)) {
    		return null;
    	}
    	$row = mysql_fetch_assoc($result);
    	return $row['ID'];
    }
    
    function getPageMonitored($email) {
    	if(!$this->DBLogin()) {
    		$this->HandleError("Database login failed!");
    		return false;
    	}
    	
    	$qry = "SELECT PM.* FROM PM_PAGEMONITORED PM WHERE PM.ID = ".$this->getPageMonitoredId($email)."";
    	$result = mysql_query($qry, $this->connection);
    	if((!$result) || (mysql_num_rows($result) != 1)) {
    		return null;
    	}
    	$row = mysql_fetch_assoc($result);
    	return $row;    	
    }
    
    function IsPageRegisteredFieldUnique($fieldValue, $fieldName) {
    	$qry = "select username from PM_PAGEMONITORED where $fieldName='".$fieldValue."'";
    	$result = mysql_query($qry, $this->connection);
    	if($result && mysql_num_rows($result) > 0) {
    		return false;
    	}
    	return true;
    }
    
    function collectPageMonitoredSubmission() {
    	$formvars = array();
    	 
    	$formvars['monitoredUrl'] = $this->Sanitize($_POST['monitoredUrl']);
    	$formvars['checkFrequencyMin'] = $this->Sanitize($_POST['checkFrequencyMin']);
    	
    	return $formvars;
    }
    
    function RegisterUser() {
        if(!isset($_POST['submitted'])) {
           return false;
        }
        
        if (strcmp($this->Sanitize($_POST['email']), "ricardo.castiglione@gmail.com") != 0) {
        	$this->HandleError("That email can't be registered");
        	return false;
        }
        
        if(!$this->ValidateRegistrationSubmission()) {
            return false;
        }
        
        $formvars = $this->CollectRegistrationSubmission();
        
        if(!$this->SaveUserToDatabase($formvars)) {
            return false;
        }
        
        if(!$this->SendUserConfirmationEmail($formvars)) {
            return false;
        }

        $this->SendAdminIntimationEmail($formvars);
        
        return true;
    }

    function ConfirmUser() {
        if(empty($_GET['code'])||strlen($_GET['code'])<=10) {
            $this->HandleError("Please provide the confirm code");
            return false;
        }
        $user_rec = array();
        if(!$this->UpdateDBRecForConfirmation($user_rec)) {
            return false;
        }
        
        $this->SendUserWelcomeEmail($user_rec);
        
        $this->SendAdminIntimationOnRegComplete($user_rec);
        
        return true;
    }    
    
    function Login() {
        if(empty($_POST['username'])) {
            $this->HandleError("UserName is empty!");
            return false;
        }
        
        if(empty($_POST['password'])) {
            $this->HandleError("Password is empty!");
            return false;
        }
        
        $username = trim($_POST['username']);
        $password = trim($_POST['password']);
        
        if(!isset($_SESSION)){ session_start(); }
        if(!$this->checkLoginInDB($username,$password)) {
            return false;
        }
        
        $_SESSION[$this->GetLoginSessionVar()] = $username;
        
        return true;
    }
    
    function checkLogin() {
         if(!isset($_SESSION)){ session_start(); }

         $sessionvar = $this->GetLoginSessionVar();
         
         if(empty($_SESSION[$sessionvar])) {
            return false;
         }
         return true;
    }
    
    function UserFullName() {
        return isset($_SESSION['name_of_user'])?$_SESSION['name_of_user']:'';
    }
    
    function getUserEmail() {
        return isset($_SESSION['email_of_user'])?$_SESSION['email_of_user']:'';
    }
    
    function LogOut() {
        session_start();
        
        $sessionvar = $this->GetLoginSessionVar();
        
        $_SESSION[$sessionvar]=NULL;
        
        unset($_SESSION[$sessionvar]);
    }
    
    function EmailResetPasswordLink() {
        if(empty($_POST['email'])) {
            $this->HandleError("Email is empty!");
            return false;
        }
        $user_rec = array();
        if(false === $this->GetUserFromEmail($_POST['email'], $user_rec)) {
            return false;
        }
        if(false === $this->SendResetPasswordLink($user_rec)) {
            return false;
        }
        return true;
    }
    
    function ResetPassword() {
        if(empty($_GET['email'])) {
            $this->HandleError("Email is empty!");
            return false;
        }
        if(empty($_GET['code']))
        {
            $this->HandleError("reset code is empty!");
            return false;
        }
        $email = trim($_GET['email']);
        $code = trim($_GET['code']);
        
        if($this->GetResetPasswordCode($email) != $code)
        {
            $this->HandleError("Bad reset code!");
            return false;
        }
        
        $user_rec = array();
        if(!$this->GetUserFromEmail($email,$user_rec))
        {
            return false;
        }
        
        $new_password = $this->ResetUserPasswordInDB($user_rec);
        if(false === $new_password || empty($new_password))
        {
            $this->HandleError("Error updating new password");
            return false;
        }
        
        if(false == $this->SendNewPassword($user_rec,$new_password))
        {
            $this->HandleError("Error sending new password");
            return false;
        }
        return true;
    }
    
    function ChangePassword() {
        if(!$this->checkLogin()) {
            $this->HandleError("Not logged in!");
            return false;
        }
        
        if(empty($_POST['oldpwd'])) {
            $this->HandleError("Old password is empty!");
            return false;
        }
        
        if(empty($_POST['newpwd'])) {
            $this->HandleError("New password is empty!");
            return false;
        }
        
        $user_rec = array();
        if(!$this->GetUserFromEmail($this->getUserEmail(),$user_rec)) {
            return false;
        }
        
        $pwd = trim($_POST['oldpwd']);
        
        if($user_rec['password'] != md5($pwd)) {
            $this->HandleError("The old password does not match!");
            return false;
        }
        $newpwd = trim($_POST['newpwd']);
        
        if(!$this->ChangePasswordInDB($user_rec, $newpwd)) {
            return false;
        }
        return true;
    }
    
    //-------Public Helper functions -------------
    function GetSelfScript() {
        return htmlentities($_SERVER['PHP_SELF']);
    }    
    
    function safeDisplay($value_name) {
        if(empty($_POST[$value_name])) {
            return'';
        }
        return htmlentities($_POST[$value_name]);
    }
    
    function redirectToURL($url) {
        header("Location: $url");
        exit;
    }
    
    function GetSpamTrapInputName() {
        return 'sp'.md5('KHGdnbvsgst'.$this->rand_key);
    }
    
    function GetErrorMessage() {
        if(empty($this->error_message)) {
            return '';
        }
        $errormsg = nl2br(htmlentities($this->error_message));
        return $errormsg;
    }    
    //-------Private Helper functions-----------
    
    function HandleError($err) {
        $this->error_message .= $err."\r\n";
    }
    
    function HandleDBError($err) {
        $this->HandleError($err."\r\n mysqlerror:".mysql_error());
    }
    
    function GetFromAddress() {
        if(!empty($this->from_address)) {
            return $this->from_address;
        }

        $host = $_SERVER['SERVER_NAME'];

        $from ="nobody@$host";
        return $from;
    } 
    
    function GetLoginSessionVar() {
        $retvar = md5($this->rand_key);
        $retvar = 'usr_'.substr($retvar,0,10);
        return $retvar;
    }
    
    function checkLoginInDB($username,$password) {
        if(!$this->DBLogin()) {
            $this->HandleError("Database login failed!");
            return false;
        }          
        $username = $this->SanitizeForSQL($username);
        $pwdmd5 = md5($password);
        $qry = "Select name, email from $this->tablename where username='$username' and password='$pwdmd5' and confirmcode='y'";
        
        $result = mysql_query($qry,$this->connection);
        
        if(!$result || mysql_num_rows($result) <= 0) { 
            $this->HandleError("Error logging in. The username or password does not match");
            return false;
        }
        
        $row = mysql_fetch_assoc($result);
        
        
        $_SESSION['name_of_user']  = $row['name'];
        $_SESSION['email_of_user'] = $row['email'];
        
        return true;
    }
    
    function UpdateDBRecForConfirmation(&$user_rec) {
        if(!$this->DBLogin()) {
            $this->HandleError("Database login failed!");
            return false;
        }   
        $confirmcode = $this->SanitizeForSQL($_GET['code']);
        
        $result = mysql_query("Select name, email from $this->tablename where confirmcode='$confirmcode'",$this->connection);   
        if(!$result || mysql_num_rows($result) <= 0) {
            $this->HandleError("Wrong confirm code.");
            return false;
        }
        $row = mysql_fetch_assoc($result);
        $user_rec['name'] = $row['name'];
        $user_rec['email']= $row['email'];
        
        $qry = "Update $this->tablename Set confirmcode='y' Where  confirmcode='$confirmcode'";
        
        if(!mysql_query( $qry ,$this->connection)) {
            $this->HandleDBError("Error inserting data to the table\nquery:$qry");
            return false;
        }      
        return true;
    }
    
    function ResetUserPasswordInDB($user_rec) {
        $new_password = substr(md5(uniqid()),0,10);
        
        if(false == $this->ChangePasswordInDB($user_rec,$new_password)) {
            return false;
        }
        return $new_password;
    }
    
    function ChangePasswordInDB($user_rec, $newpwd) {
        $newpwd = $this->SanitizeForSQL($newpwd);
        
        $qry = "Update $this->tablename Set password='".md5($newpwd)."' Where  id=".$user_rec['id']."";
        
        if(!mysql_query( $qry ,$this->connection)) {
            $this->HandleDBError("Error updating the password \nquery:$qry");
            return false;
        }     
        
        return true;
    }
    
    function GetUserFromEmail($email,&$user_rec) {
        if(!$this->DBLogin()) {
            $this->HandleError("Database login failed!");
            return false;
        }   
        $email = $this->SanitizeForSQL($email);
        
        $result = mysql_query("Select * from $this->tablename where email='$email'",$this->connection);  

        if(!$result || mysql_num_rows($result) <= 0) {
            $this->HandleError("There is no user with email: $email");
            return false;
        }
        $user_rec = mysql_fetch_assoc($result);

        return true;
    }
    
    function smtpmailer($to, $subject, $body) {
    	$mailer = new PHPMailer();  // create a new object
    	 
    	$mailer->CharSet = 'utf-8';
    	$mailer->IsSMTP(); // enable SMTP
    	$mailer->SMTPDebug = 0;  // debugging: 1 = errors and messages, 2 = messages only
    	$mailer->SMTPAuth = true;  // authentication enabled
    	$mailer->SMTPSecure = 'tls'; // secure transfer enabled REQUIRED for GMail
    	$mailer->Host = 'smtp.gmail.com';
    	$mailer->Port = 587;
    	$mailer->Username = 'brainfields@gmail.com';
    	$mailer->Password = 'grupo403';
    	$mailer->FromName = "Page Monitoring";
    	$mailer->From = $this->GetFromAddress();
    	$mailer->Subject = $subject;
    	$mailer->Body = $body;
    	$mailer->AddAddress($to);
    
    	return $mailer->Send();
    }
    
    function SendUserWelcomeEmail(&$user_rec)
    {
    	$to = $user_rec['email']; 
    	$subject = "Welcome to ".$this->sitename; 
    	$body = "Hello ".$user_rec['name']."\r\n\r\n".
        "Welcome! Your registration  with ".$this->sitename." is completed.\r\n".
        "\r\n".
        "Regards,\r\n".
        "Webmaster\r\n".
        $this->sitename;
        
        if(!$this->smtpmailer($to, $subject, $body))
        {
            $this->HandleError("Failed sending user welcome email.");
            return false;
        }
        return true;
    }
    
    function SendAdminIntimationOnRegComplete(&$user_rec)
    {
        if(empty($this->admin_email))
        {
            return false;
        }
        
        $to = $this->admin_email; 
        $subject = "Registration Completed: ".$user_rec['name']; 
        $body = "A new user registered at ".$this->sitename."\r\n".
        "Name: ".$user_rec['name']."\r\n".
        "Email address: ".$user_rec['email']."\r\n";
        
        if(!$this->smtpmailer($to, $subject, $body))
        {
            return false;
        }
        return true;
    }
    
    function GetResetPasswordCode($email)
    {
       return substr(md5($email.$this->sitename.$this->rand_key),0,10);
    }
    
    function SendResetPasswordLink($user_rec)
    {
    	$to = $user_rec['email']; 
    	$subject = "Your reset password request at ".$this->sitename;

        $link = $this->GetAbsoluteURLFolder().
                '/pageMonitoring/page/resetPwd/resetpwd.php?email='.
                urlencode($to).'&code='.
                urlencode($this->GetResetPasswordCode($to));

        $body = "Hello ".$user_rec['name']."\r\n\r\n".
        "There was a request to reset your password at ".$this->sitename."\r\n".
        "Please click the link below to complete the request: \r\n".$link."\r\n".
        "Regards,\r\n".
        "Webmaster\r\n".
        $this->sitename;
        
        if(!$this->smtpmailer($to, $subject, $body))
        {
            return false;
        }
        return true;
    }
    
    function SendNewPassword($user_rec, $new_password) {
    	$to = $user_rec['email'];
    	$subject = "Your new password for ".$this->sitename;
    	$body = "Hello ".$user_rec['name']."\r\n\r\n".
        "Your password is reset successfully. ".
        "Here is your updated login:\r\n".
        "username:".$user_rec['username']."\r\n".
        "password:$new_password\r\n".
        "\r\n".
        "Login here: ".$this->GetAbsoluteURLFolder()."/pageMonitoring/page/login/login.php\r\n".
        "\r\n".
        "Regards,\r\n".
        "Webmaster\r\n".
        $this->sitename;
        
        if(!$this->smtpmailer($to, $subject, $body)) {
            return false;
        }
        return true;
    }    
    
    function ValidateRegistrationSubmission() {
        //This is a hidden input field. Humans won't fill this field.
        if(!empty($_POST[$this->GetSpamTrapInputName()])) {
            //The proper error is not given intentionally
            $this->HandleError("Automated submission prevention: case 2 failed");
            return false;
        }
        
        $validator = new FormValidator();
        $validator->addValidation("name","req","Please fill in Name");
        $validator->addValidation("email","email","The input for Email should be a valid email value");
        $validator->addValidation("email","req","Please fill in Email");
        $validator->addValidation("username","req","Please fill in UserName");
        $validator->addValidation("password","req","Please fill in Password");

        if(!$validator->ValidateForm()) {
            $error='';
            $error_hash = $validator->GetErrors();
            foreach($error_hash as $inpname => $inp_err)
            {
                $error .= $inpname.':'.$inp_err."\n";
            }
            $this->HandleError($error);
            return false;
        }        

        return true;
    }
    
    function CollectRegistrationSubmission() {
    	$formvars = array();
    	
        $formvars['name'] = $this->Sanitize($_POST['name']);
        $formvars['email'] = $this->Sanitize($_POST['email']);
        $formvars['username'] = $this->Sanitize($_POST['username']);
        $formvars['password'] = $this->Sanitize($_POST['password']);
        
        return $formvars;
    }
    
    function SendUserConfirmationEmail(&$formvars)
    {
    	$to = $formvars['email']; 
    	$subject = "Your registration with ".$this->sitename;
    	
        $confirmcode = $formvars['confirmcode'];
        $confirm_url = $this->GetAbsoluteURLFolder().'/pageMonitoring/page/register/confirmreg.php?code='.$confirmcode;
     
        $body = "Hello ".$formvars['name']."\r\n\r\n".
        "Thanks for your registration with ".$this->sitename."\r\n".
        "Please click the link below to confirm your registration.\r\n".
        "$confirm_url\r\n".
        "\r\n".
        "Regards,\r\n".
        "Webmaster\r\n".
        $this->sitename;;

        if(!$this->smtpmailer($to, $subject, $body))
        {
            $this->HandleError("Failed sending registration confirmation email.");
            return false;
        }
        return true;
    }
    
    function GetAbsoluteURLFolder()
    {
        $scriptFolder = (isset($_SERVER['HTTPS']) && ($_SERVER['HTTPS'] == 'on')) ? 'https://' : 'http://';
        $scriptFolder .= $_SERVER['HTTP_HOST'];
//          . dirname($_SERVER['REQUEST_URI']);
        return $scriptFolder;
    }
    
    function SendAdminIntimationEmail(&$formvars)
    {
        if(empty($this->admin_email))
        {
            return false;
        }
        
        $to = $this->admin_email; 
        $subject = "New registration: ".$formvars['name']; 
        $body = "A new user registered at ".$this->sitename."\r\n".
        "Name: ".$formvars['name']."\r\n".
        "Email address: ".$formvars['email']."\r\n".
        "UserName: ".$formvars['username'];
        
        if(!$this->smtpmailer($to, $subject, $body))
        {
            return false;
        }
        return true;
    }
    
    function SaveUserToDatabase(&$formvars) {
        if(!$this->DBLogin()) {
            $this->HandleError("Database login failed!");
            return false;
        }
        
        if(!$this->IsUserFieldUnique($formvars,'email')) {
            $this->HandleError("This email is already registered");
            return false;
        }
        
        if(!$this->IsUserFieldUnique($formvars,'username')) {
            $this->HandleError("This UserName is already used. Please try another username");
            return false;
        }        
        
        if(!$this->InsertUserIntoDB($formvars)) {
            $this->HandleError("Inserting to Database failed!");
            return false;
        }
        
        return true;
    }
    
    function IsUserFieldUnique($formvars,$fieldname) {
        $field_val = $this->SanitizeForSQL($formvars[$fieldname]);
        $qry = "select username from $this->tablename where $fieldname='".$field_val."'";
        $result = mysql_query($qry,$this->connection);   
        if($result && mysql_num_rows($result) > 0) {
            return false;
        }
        return true;
    }
    
    function DBLogin() {
        $this->connection = mysql_connect($this->db_host,$this->username,$this->pwd);

        if(!$this->connection) {   
            $this->HandleDBError("Database Login failed! Please make sure that the DB login credentials provided are correct");
            return false;
        }
        
        if(!mysql_select_db($this->database, $this->connection)) {
            $this->HandleDBError('Failed to select database: '.$this->database.' Please make sure that the database name provided is correct');
            return false;
        }
        
        if(!mysql_query("SET NAMES 'UTF8'",$this->connection)) {
            $this->HandleDBError('Error setting utf8 encoding');
            return false;
        }
        
        return true;
    }    
    
    function InsertUserIntoDB(&$formvars) {
        $confirmcode = $this->MakeConfirmationMd5($formvars['email']);
        
        $formvars['confirmcode'] = $confirmcode;
        
        $insert_query = 'insert into '.$this->tablename.'(
                name,
                email,
                username,
                password,
                confirmcode
                )
                values
                (
                "' . $this->SanitizeForSQL($formvars['name']) . '",
                "' . $this->SanitizeForSQL($formvars['email']) . '",
                "' . $this->SanitizeForSQL($formvars['username']) . '",
                "' . md5($formvars['password']) . '",
                "' . $confirmcode . '"
                )';      
        if(!mysql_query( $insert_query ,$this->connection)) {
            $this->HandleDBError("Error inserting data to the table\nquery:$insert_query");
            return false;
        }        
        return true;
    }
    
    function MakeConfirmationMd5($email) {
        $randno1 = rand();
        $randno2 = rand();
        return md5($email.$this->rand_key.$randno1.''.$randno2);
    }
    
    function SanitizeForSQL($str) {
        if( function_exists( "mysql_real_escape_string" ) ) {
              $ret_str = mysql_real_escape_string( $str );
        } else {
              $ret_str = addslashes( $str );
        }
        return $ret_str;
    }
    
 /*
    Sanitize() function removes any potential threat from the
    data submitted. Prevents email injections or any other hacker attempts.
    if $remove_nl is true, newline chracters are removed from the input.
    */
    function Sanitize($str,$remove_nl=true) {
        $str = $this->StripSlashes($str);

        if($remove_nl) {
            $injections = array('/(\n+)/i',
                '/(\r+)/i',
                '/(\t+)/i',
                '/(%0A+)/i',
                '/(%0D+)/i',
                '/(%08+)/i',
                '/(%09+)/i'
                );
            $str = preg_replace($injections,'',$str);
        }

        return $str;
    }
        
    function StripSlashes($str) {
        if(get_magic_quotes_gpc()) {
            $str = stripslashes($str);
        }
        return $str;
    }
}
?>