<?php
/*
This file is really development code but might help out anyone looking to
implement the same same thing. Feel free to use and any links back to my site
would be welcome as would notification of any changes/updates. In particular,
any improvements implementing transparency, new block patterns and re-factoring
would be welcome.

Charles Darke
http://digitalconsumption.com

Please keep this notice on re-distributions.
*/

define ("VISIUPLOAD",'/yourdownloaddirectoryhere/');
define ("VISIGLYPH",'replacewithaverylongrandomstring');

function glyph($blocksize,$i,$j,$k,$rot1,$rot2,$fgr,$fgg,$fgb,$fgr2,$fgg2,$fgb2,$bgr,$bgg,$bgb,$filename){

    // set a minimum blocksize below which we draw bigger and then resample downwards for a better look
    $resize=0;
    $minblocksize=24;
    if ($blocksize<$minblocksize){
        $resize=$blocksize;
        $blocksize=$minblocksize;
    }

    $imgsize=$blocksize*3;
    $quarter=$blocksize/4;
    $quarter3=$quarter*3;
    $half=$blocksize/2;
    $third=$blocksize/3;
    $centre=$imgsize/2;

    $temp_block = imagecreatetruecolor($blocksize*2,$blocksize);
    $rotate_temp = imagecreate($blocksize,$blocksize);
    $im = imagecreate($imgsize,$imgsize);

    //$backgroundcolor = imagecolorallocate($im, 255,255,255);
    //$backgroundcolor = imagecolorallocate($temp_block, 255, 255, 255);
    //$backgroundcolor = imagecolorallocate($rotate_temp, 255, 255, 255);
    $backgroundcolor = imagecolorallocate($im, $bgr, $bgg, $bgb);
    $backgroundcolor = imagecolorallocate($temp_block, $bgr, $bgg, $bgb);
    $backgroundcolor = imagecolorallocate($rotate_temp, $bgr, $bgg, $bgb);
    $red = imagecolorallocate($im, $fgr, $fgg, $fgb);

    $originx=0;
    $originy=0;

    // Draw first pattern using extracted function
    drawPatternOnCanvas($im, $i, $originx, $originy, $blocksize, $red, $quarter, $quarter3, $half);

    // rotate block
    imagecopy ($rotate_temp, $im, 0, 0, 0, 0, $blocksize, $blocksize);
    $rotation1=90*$rot1;
    $rotate_temp=imagerotate ( $rotate_temp, $rotation1, $backgroundcolor);
    imagecopy ($im, $rotate_temp, 0, 0, 0, 0, $blocksize, $blocksize);


    $originx=$blocksize;
    $originy=0;
    $red = imagecolorallocate($im, $fgr2, $fgg2, $fgb2);

    // Draw second pattern using extracted function
    drawPatternOnCanvas($im, $j, $originx, $originy, $blocksize, $red, $quarter, $quarter3, $half);

    // rotate block
    imagecopy ($rotate_temp, $im, 0, 0, $originx, 0, $blocksize, $blocksize);
    $rotation1=90*$rot2;
    $rotate_temp=imagerotate ( $rotate_temp, $rotation1, $backgroundcolor);
    imagecopy ($im, $rotate_temp, $originx, 0, 0, 0, $blocksize, $blocksize);

    // copy blocks to form radial pattern
    for ($roundabout=0;$roundabout<3;$roundabout++){
        // copy blocks
        imagecopy ($temp_block, $im, 0, 0, 0, 0, $blocksize*2, $blocksize);

        // rotate
        $im=imagerotate ( $im, 90, $backgroundcolor);

        // paste back
        imagecopy ($im, $temp_block, 0, 0, 0, 0, $blocksize*2, $blocksize);
    }

    $red = imagecolorallocate($im, $fgr, $fgg, $fgb);

    $originx=$blocksize;
    $originy=$blocksize;
    // draw centre

    switch($k){
        case 1: // circle
            imagefilledellipse($im, $centre, $centre, $quarter3, $quarter3, $red);
            break;

        case 2: // quarter square
            imagefilledrectangle ( $im, $originx+$quarter, $originy+$quarter, $originx+$quarter3, $originy+$quarter3, $red);
            break;

        case 3: // full square
            imagefilledrectangle ( $im, $originx, $originy, $originx+$blocksize, $originy+$blocksize, $red);
            break;

        case 4: // quarter diamond
            $points = array(
                $originx+$half, $originy+$quarter,
                $originx+$quarter3, $originy+$half,
                $originx+$half, $originy+$quarter3,
                $originx+$quarter, $originy+$half
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            break;
            
        case 5: // diamond
            $points = array(
                $originx+$half, $originy,
                $originx, $originy+$half,
                $originx+$half, $originy+$blocksize,
                $originx+$blocksize, $originy+$half
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            break;

        default:
            // empty space

    }

    header('Content-type: image/png');

    if ($resize>0){ // if we need to resample down
        $blocksize=$resize;
        $imgsizeR=$blocksize*3;
        $imresize = imagecreatetruecolor($imgsizeR,$imgsizeR);
        $backgroundcolor = imagecolorallocate($imresize, $bgr, $bgg, $bgb);
        imagecopyresampled ( $imresize, $im, 0, 0, 0, 0, $imgsizeR, $imgsizeR, $imgsize, $imgsize );
        //ImageColorTransparent($imresize,$backgroundcolor); // FIXME transparency feature not finished.
        imagepng($imresize);
        //imagepng($imresize,VISIUPLOAD.$filename.'.png'); // FIXME remove comments to generate file cache
    } else {
        imagepng($im);
        //imagepng($im,VISIUPLOAD.$filename.'.png');// FIXME remove comments to generate file cache
    }
}

function drawPatternOnCanvas($im, $patternType, $originx, $originy, $blocksize, $red, $quarter, $quarter3, $half) {
    switch($patternType){
        case 1: // #1 mountains
            $points = array(
                $originx, $originy,
                $originx+$quarter, $originy+$blocksize,
                $originx+$half, $originy
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            $points = array(
                $originx+$half, $originy,
                $originx+$quarter3, $originy+$blocksize,
                $originx+$blocksize, $originy
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            break;

        case 2: // #2 half triangle
            $points = array(
                $originx, $originy,
                $originx+$blocksize, $originy,
                $originx, $originy+$blocksize
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            break;

        case 3: // #3 centre triangle
            $points = array(
                $originx, $originy,
                $originx+$half, $originy+$blocksize,
                $originx+$blocksize, $originy
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            break;

        case 4: // #4 half block
            imagefilledrectangle ( $im, $originx, $originy, $originx+$half, $originy+$blocksize, $red);
            break;

        case 5: // #5 half diamond
            $points = array(
                $originx+$quarter, $originy,
                $originx, $originy+$half,
                $originx+$quarter, $originy+$blocksize,
                $originx+$half, $originy+$half
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            break;

        case 6: // #6 spike
            $points = array(
                $originx, $originy,
                $originx+$blocksize, $originy+$half,
                $originx+$blocksize, $originy+$blocksize,
                $originx+$half, $originy+$blocksize
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            break;

        case 7: // #7 quarter triangle
            $points = array(
                $originx, $originy,
                $originx+$half, $originy+$blocksize,
                $originx, $originy+$blocksize
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            break;

        case 8: // #8 diag triangle
            $points = array(
                $originx, $originy,
                $originx+$blocksize, $originy+$half,
                $originx+$half, $originy+$blocksize
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            break;

        case 9: // #9 centre mini triangle
            $points = array(
                $originx+$quarter, $originy+$quarter,
                $originx+$quarter3, $originy+$quarter,
                $originx+$quarter, $originy+$quarter3
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            break;

        case 10: // #10 diag mountains
            $points = array(
                $originx, $originy,
                $originx+$half, $originy,
                $originx+$half, $originy+$half
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            $points = array(
                $originx+$half, $originy+$half,
                $originx+$blocksize, $originy+$half,
                $originx+$blocksize, $originy+$blocksize
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            break;

        case 11: // #11 quarter block
            imagefilledrectangle ( $im, $originx, $originy, $originx+$half, $originy+$half, $red);
            break;

        case 12: // #12 point out triangle
            $points = array(
                $originx, $originy+$half,
                $originx+$half, $originy+$blocksize,
                $originx+$blocksize, $originy+$half
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            break;

        case 13: // #13 point in triangle
            $points = array(
                $originx, $originy,
                $originx+$half, $originy+$half,
                $originx+$blocksize, $originy
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            break;

        case 14: // #14 diag point in
            $points = array(
                $originx+$half, $originy+$half,
                $originx, $originy+$half,
                $originx+$half, $originy+$blocksize
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            break;

        case 15: // #15 diag point out
            $points = array(
                $originx, $originy,
                $originx+$half, $originy,
                $originx, $originy+$half,
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
            break;

        case 16:	// #16 diag side point out
        default:
            $points = array(
                $originx, $originy,
                $originx+$half, $originy,
                $originx+$half, $originy+$half
            );
            $num = count($points) / 2;
            imagefilledpolygon($im, $points, $num, $red);
    } // end switch
    
}
$size=24;
$filename='test';
$ip=md5(rand(0,300)); // FIXME test random
	glyph(
		$size,
		hexdec(substr($ip,0,1)), //block 1
		hexdec(substr($ip,1,1)), //block 2
		hexdec(substr($ip,2,1))&7, //centre
		hexdec(substr($ip,3,1))&3, //rot 1
		hexdec(substr($ip,4,1))&3, //rot 2
		hexdec(substr($ip, 5,2))&239, //fg
		hexdec(substr($ip, 7,2))&239, 
		hexdec(substr($ip, 9,2))&239, 
		hexdec(substr($ip,11,2))&239, //fg2
		hexdec(substr($ip,13,2))&239, 
		hexdec(substr($ip,15,2))&239, 
		255,255,255,
		//hexdec(substr($ip,17,2)), //bg
		//hexdec(substr($ip,19,2)),
		//hexdec(substr($ip,21,2)),
		$filename
		);

?>
