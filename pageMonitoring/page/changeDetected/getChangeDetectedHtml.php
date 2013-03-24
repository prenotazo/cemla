<?php
require_once("./../../include/membersite_config.php");

$changeDetectedId = $_GET["changeDetectedId"];
$changeDetected = $fgmembersite->getChangeDetected($changeDetectedId);
$htmlText = $changeDetected['html'];
echo "<textarea style='width: 100%; height: 100%; resize: none;' wrap='off' disabled='disabled'>";
echo $htmlText;
echo "</textarea>";
?>