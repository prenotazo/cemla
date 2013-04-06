<?php
require_once("./../../include/membersite_config.php");

$changeDetectedId = $_GET["changeDetectedId"];
$diff = $fgmembersite->getDiffWithPrevious($changeDetectedId);
#echo "<textarea style='width: 100%; height: 100%; resize: none;' wrap='on' disabled='disabled'>";
echo $diff;
#echo "</textarea>";
?>