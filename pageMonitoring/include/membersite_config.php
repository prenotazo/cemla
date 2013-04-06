<?PHP
require_once("./../../include/fg_membersite.php");

$fgmembersite = new FGMembersite();

//Provide your site name here
$fgmembersite->SetWebsiteName('ricardocastiglione.com.ar');

//Provide the email address where you want to get notifications
$fgmembersite->SetAdminEmail('ricardo.castiglione@gmail.com');

//Provide your database login details here:
//hostname, user name, password, database name and table name
//note that the script will create the table (for example, fgusers in this case)
//by itself on submitting register.php for the first time
$fgmembersite->InitDB(/*dev hostname'localhost',*/
					  /*prod hostname*/'www.pagemonitoring.com.ar',
                      /*username*/'pagemoni_pm',
                      /*password*/'pm2013',
                      /*database name*/'pagemoni_pm',
                      /*table name*/'PM_USER');

//For better security. Get a random string from this link: http://tinyurl.com/randstr
// and put it here
$fgmembersite->SetRandomKey('fisjpjZoGVQTnAw');

?>