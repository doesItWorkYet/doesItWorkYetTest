package org.firstinspires.ftc.teamcode;



import org.lasarobotics.vision.android.Cameras;
import org.lasarobotics.vision.ftc.resq.Beacon;
import org.lasarobotics.vision.opmode.LinearVisionOpMode;
import org.lasarobotics.vision.opmode.extensions.CameraControlExtension;
import org.lasarobotics.vision.util.ScreenOrientation;
import org.opencv.core.Point;
import org.opencv.core.Size;

/**
 * Created by root on 12/23/16.
 */
public class beaconCapRedTeamAutonomous2 extends LinearVisionOpMode {
    private double redTolerance = 0;
    private double blueTolerance = 0;
    HardwareMapLucyV4 robot;

    @Override
    public void runOpMode() throws InterruptedException {
        //initialize hardware map
        robot = new HardwareMapLucyV4();
        robot.init(hardwareMap);
        robot.zero();
        waitForStart();
        //dirve forward from wall
        telemetry.addData("Action", "Driving Forward");
        telemetry.update();

        robot.driveDistance(1*robot.FOOT_TO_METERS, .5);
        //turn to approach wall
        telemetry.addData("Action", "Turning");
        telemetry.update();
        robot.turnToDegree(45);
        //got to white line
        telemetry.addData("Action", "Moving until white detected");
        telemetry.update();
        robot.goForwardUntilWhite(robot.USE_BRIGHTNESS);
        //turn to face wall
        telemetry.addData("Action", "Turning");
        telemetry.update();
        robot.turnToDegree(45);
        //determine which side is red
        int redSide = getRedSide();
        if(redSide == robot.BEACON_RED){
            telemetry.addData("Beacon on Right is Team: ", "RED");
        }
        else telemetry.addData("Beacon on Right is Team: ", "BLUE");
        telemetry.update();
        //go until the beacon is captured
        while(!isBeaconCaptured()) {
            telemetry.addData("Beacon Captured Status: " , "False");
            telemetry.update();
            if (redSide == robot.BEACON_RED)
                robot.beaconApproach(robot.BEACON_RIGHT);
            else robot.beaconApproach(robot.BEACON_LEFT);
            //if not still captured, back up and try again
            if(!isBeaconCaptured()){
                robot.driveDistance(-.5, .5);
            }
        }
        telemetry.addData("Beacon Captured Status: " , "True");

        //back up, turn and approach next

        robot.driveDistance(-1,.5);
        robot.turnToDegree(-90);
        /*
        robot.goForwardUntilWhiteRGB();
        robot.turnToDegree(90);
        redSide = getRedSide();
        //go until the beacon is captured
        while(!isBeaconCaptured()) {
            if (redSide == robot.BEACON_RED)
                robot.beaconApproach(robot.BEACON_RIGHT);
            else robot.beaconApproach(robot.BEACON_LEFT);
            //if not still captured, back up and try again
            if(!isBeaconCaptured()){
                robot.driveDistance(-.5);
            }
        }
        robot.driveDistance(-1);
        */




    }

    public int getRedSide(){
        //determine which side is red
        int leftSideColor = getLeftColor();
        if(leftSideColor == robot.BEACON_RED) return robot.BEACON_LEFT;
        else return  robot.BEACON_RIGHT;
    }

    public void beginDetection(double red, double blue){
        this.redTolerance = red;
        this.blueTolerance = blue;
        telemetry.addData("Vars", "Set");
        telemetry.update();
        try {
            waitForVisionStart();
        }
        catch (Exception e){
            telemetry.addData("Exception: ", e.getMessage());
        }

        super.init();
        /**
         * Set the camera used for detection
         * PRIMARY = Front-facing, larger camera
         * SECONDARY = Screen-facing, "selfie" camera :D
         **/
        this.setCamera(Cameras.PRIMARY);
        telemetry.addData("Cameras", "Set");
        telemetry.update();
        /**
         * Set the frame size
         * Larger = sometimes more accurate, but also much slower
         * After this method runs, it will set the "width" and "height" of the frame
         **/
        this.setFrameSize(new Size(900, 900));
        telemetry.addData("Frame", "set");
        telemetry.update();
        /**
         * Enable extensions. Use what you need.
         * If you turn on the BEACON extension, it's best to turn on ROTATION too.
         */
        enableExtension(Extensions.BEACON);         //Beacon detection
        enableExtension(Extensions.ROTATION);       //Automatic screen rotation correction
        enableExtension(Extensions.CAMERA_CONTROL); //Manual camera control

        /**
         * Set the beacon analysis method
         * Try them all and see what works!
         */
        beacon.setAnalysisMethod(Beacon.AnalysisMethod.FAST);

        /**
         * Set color tolerances
         * 0 is default, -1 is minimum and 1 is maximum tolerance
         */
        beacon.setColorToleranceRed(redTolerance);
        beacon.setColorToleranceBlue(blueTolerance);

        /**
         * Set analysis boundary
         * You should comment this to use the entire screen and uncomment only if
         * you want faster analysis at the cost of not using the entire frame.
         * This is also particularly useful if you know approximately where the beacon is
         * as this will eliminate parts of the frame which may cause problems
         * This will not work on some methods, such as COMPLEX
         **/
        //beacon.setAnalysisBounds(new Rectangle(new Point(width / 2, height / 2), width - 200, 200));

        /**
         * Set the rotation parameters of the screen
         * If colors are being flipped or output appears consistently incorrect, try changing these.
         *
         * First, tell the extension whether you are using a secondary camera
         * (or in some devices, a front-facing camera that reverses some colors).
         *
         * It's a good idea to disable global auto rotate in Android settings. You can do this
         * by calling disableAutoRotate() or enableAutoRotate().
         *
         * It's also a good idea to force the phone into a specific Orientation (or auto rotate) by
         * calling either setActivityOrientationAutoRotate() or setActivityOrientationFixed(). If
         * you don't, the camera reader may have problems reading the current Orientation.
         */
        rotation.setIsUsingSecondaryCamera(false);
        rotation.disableAutoRotate();
        rotation.setActivityOrientationFixed(ScreenOrientation.PORTRAIT);

        /**
         * Set camera control extension preferences
         *
         * Enabling manual settings will improve analysis rate and may lead to better results under
         * tested conditions. If the environment changes, expect to change these values.
         */
        cameraControl.setColorTemperature(CameraControlExtension.ColorTemperature.AUTO);
        cameraControl.setAutoExposureCompensation();
    }

    private Beacon.BeaconAnalysis getAnalysis(){
        return beacon.getAnalysis();
    }

    public Point getBeaconCenter(){
        return getAnalysis().getCenter();
    }

    public Point[] getBeaconButtonLocations(){
        Point[] toReturn = {getAnalysis().getLeftButton().center(), getAnalysis().getRightButton().center()};
        return toReturn;
    }

    public boolean isBeaconCaptured(){
        if(getAnalysis().isLeftBlue() && getAnalysis().isRightBlue())  return true;
        if(getAnalysis().isLeftRed() && getAnalysis().isRightRed()) return true;
        return false;
    }

    public int getLeftColor(){
        if(getAnalysis().isLeftBlue()) return robot.BEACON_BLUE;
        if(getAnalysis().isLeftRed()) return robot.BEACON_RED;
        else return 0;
    }


    public int getRightColor(){
        if(getAnalysis().isRightBlue()) return robot.BEACON_BLUE;
        if(getAnalysis().isRightRed()) return robot.BEACON_RED;
        else return 0;
    }
}
