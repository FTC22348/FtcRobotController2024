package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.hardware.bosch.BNO055IMU;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.vision.tfod.TfodProcessor;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

@Autonomous(name = "RED AUTON STATES", group = "Concept")

public class AUTONRED_States extends LinearOpMode{

 
    private boolean hasTargets;
    private int generatedScenario;

    DcMotor motorFrontLeft;
    DcMotor motorBackLeft;
    DcMotor motorFrontRight;
    DcMotor motorBackRight;
    Servo autonServo;

    BNO055IMU imu;
    Orientation lastAngles = new Orientation();
    double globalAngle;

    private ElapsedTime runtime = new ElapsedTime();

    static final double COUNTS_PER_MOTOR_REV = 538;
    static final double DRIVE_GEAR_REDUCTION = 1.75;
    static final double WHEEL_DIAMETER_INCHES = 3.78;
    static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.14159);
    static final double DRIVE_SPEED = 0.6;
    static final double TURN_SPEED = 0.2;

    private static final boolean USE_WEBCAM = true;  // true for webcam, false for phone camera

    // TFOD_MODEL_ASSET points to a model file stored in the project Asset location,
    // this is only used for Android Studio when using models in Assets.
    private static final String TFOD_MODEL_ASSET = "redTest1.tflite";
    // TFOD_MODEL_FILE points to a model file stored onboard the Robot Controller's storage,
    // this is used when uploading models directly to the RC using the model upload interface.
    private static final String TFOD_MODEL_FILE = "/sdcard/FIRST/tflitemodels/redTest1.tflite";
    // Define the labels recognized in the model for TFOD (must be in training order!)
    private static final String[] LABELS = {
       "red",
    };
    
    
    private double targetX;

    /**
     * The variable to store our instance of the TensorFlow Object Detection processor.
     */
    private TfodProcessor tfod;

    /**
     * The variable to store our instance of the vision portal.
     */
    private VisionPortal visionPortal;

    @Override
    public void runOpMode() {
        
        // Initialize the IMU and its parameters.
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json";
        parameters.loggingEnabled = true;
        parameters.loggingTag = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        motorFrontLeft = hardwareMap.dcMotor.get("motorFrontLeft");
        motorBackLeft = hardwareMap.dcMotor.get("motorBackLeft");
        motorFrontRight = hardwareMap.dcMotor.get("motorFrontRight");
        motorBackRight = hardwareMap.dcMotor.get("motorBackRight");

        motorFrontLeft.setDirection(DcMotor.Direction.REVERSE);
        motorBackLeft.setDirection(DcMotor.Direction.REVERSE);
        // motorBackRight.setDirection(DcMotor.Direction.REVERSE); // On expansion hub,
        // also needs to be reversed

        motorFrontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBackLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorFrontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        motorBackRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // The IMU does not initialize instantly. This makes it so the driver can see
        // when they can push Play without errors.
        telemetry.addData("Mode", "calibrating...");
        telemetry.update();
        while (!isStopRequested() && !imu.isGyroCalibrated()) {
            sleep(50);
            idle();
        }

        telemetry.addData("Status", "Resetting Encoders");
        telemetry.update();

        motorFrontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorBackLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorFrontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motorBackRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        motorFrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorFrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        motorBackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        telemetry.addData("Path0", "Starting at %7d :%7d",
                motorBackLeft.getCurrentPosition(),
                motorBackRight.getCurrentPosition(),
                motorFrontLeft.getCurrentPosition(),
                motorFrontRight.getCurrentPosition());
        telemetry.update();

        // Tells the driver it is ok to start.
        telemetry.addData("Mode", "waiting for start");
        telemetry.addData("imu calib status", imu.getCalibrationStatus().toString());
        telemetry.update();

        initTfod();

        // Wait for the DS start button to be touched.
        telemetry.addData("DS preview on/off", "3 dots, Camera Stream");
        telemetry.addData(">", "Touch Play to start OpMode");
        telemetry.update();
        waitForStart();
        
        //INSERT ENCODER DRIVE METHODS HERE!!!
        //frontLeft, frontRight, backLeft, backRight
        sleep(4000);
        telemetryTfod();
        telemetryTfod();
        telemetryTfod();
        encoderDrive(DRIVE_SPEED, 3, 3, 3, 3, 0.5);
        if(hasTargets){
            //DOWN THE MIDDLE
            encoderDrive(DRIVE_SPEED, 21, 21, 21, 21, 5);
            encoderDrive(DRIVE_SPEED, -3, -3, -3, -3, 5);
        } else {
            encoderDrive(DRIVE_SPEED, -6, 6, 6, -6, 1);
            sleep(4000);
            telemetryTfod();
            telemetryTfod();
            telemetryTfod();
            if(hasTargets){
                encoderDrive(DRIVE_SPEED, 6, -6, -6, 6, 1);
                encoderDrive(DRIVE_SPEED, 14, 14, 14, 14, 1);
                rotate(90,TURN_SPEED);
                encoderDrive(DRIVE_SPEED, -5, -5, -5, -5, 1);
                encoderDrive(DRIVE_SPEED, 5, -5, -5, 5, 1);
                encoderDrive(DRIVE_SPEED, 8, 8, 8, 8, 1);
                encoderDrive(DRIVE_SPEED, -3, -3, -3, -3, 5);
                //TO THE LEFT
            } else {
                encoderDrive(DRIVE_SPEED, 6, -6, -6, 6, 1);
                encoderDrive(DRIVE_SPEED, 14, 14, 14, 14, 1);
                rotate(-80,TURN_SPEED);
                encoderDrive(DRIVE_SPEED, -7, -7, -7, -7, 1);
                encoderDrive(DRIVE_SPEED, -7, 7, 7, -7, 1);
                encoderDrive(DRIVE_SPEED, 10, 10, 10, 10, 1);
                encoderDrive(DRIVE_SPEED, -3, -3, -3, -3, 5);
                //TO THE RIGHT
            }
        }
        
        

        if (opModeIsActive()) {
            while (opModeIsActive()) {

                telemetryTfod();

                // Push telemetry to the Driver Station.
                telemetry.update();

                // Share the CPU.
                sleep(20);
            }
        }

        // Save more CPU resources when camera is no longer needed.
        visionPortal.close();

    }   // end runOpMode()

    /**
     * Initialize the TensorFlow Object Detection processor.
     */
    private void initTfod() {

        // Create the TensorFlow processor by using a builder.
        tfod = new TfodProcessor.Builder()

            // With the following lines commented out, the default TfodProcessor Builder
            // will load the default model for the season. To define a custom model to load, 
            // choose one of the following:
            //   Use setModelAssetName() if the custom TF Model is built in as an asset (AS only).
            //   Use setModelFileName() if you have downloaded a custom team model to the Robot Controller.
            //.setModelAssetName(TFOD_MODEL_ASSET)
            
            //.setModelAssetName(TFOD_MODEL_ASSET)
            .setModelFileName(TFOD_MODEL_FILE)

            // The following default settings are available to un-comment and edit as needed to 
            // set parameters for custom models.
            //.setModelLabels(LABELS)
            //.setIsModelTensorFlow2(true)
            //.setIsModelQuantized(true)
            //.setModelInputSize(300)
            //.setModelAspectRatio(16.0 / 9.0)

            .build();

        // Create the vision portal by using a builder.
        VisionPortal.Builder builder = new VisionPortal.Builder();

        // Set the camera (webcam vs. built-in RC phone camera).
        if (USE_WEBCAM) {
            builder.setCamera(hardwareMap.get(WebcamName.class, "Webcam 1"));
        } else {
            builder.setCamera(BuiltinCameraDirection.BACK);
        }

        // Choose a camera resolution. Not all cameras support all resolutions.
        //builder.setCameraResolution(new Size(640, 480));

        // Enable the RC preview (LiveView).  Set "false" to omit camera monitoring.
        //builder.enableLiveView(true);

        // Set the stream format; MJPEG uses less bandwidth than default YUY2.
        //builder.setStreamFormat(VisionPortal.StreamFormat.YUY2);

        // Choose whether or not LiveView stops if no processors are enabled.
        // If set "true", monitor shows solid orange screen if no processors enabled.
        // If set "false", monitor shows camera view without annotations.
        //builder.setAutoStopLiveView(false);

        // Set and enable the processor.
        builder.addProcessor(tfod);

        // Build the Vision Portal, using the above settings.
        visionPortal = builder.build();

        // Set confidence threshold for TFOD recognitions, at any time.
        //tfod.setMinResultConfidence(0.75f);

        // Disable or re-enable the TFOD processor at any time.
        //visionPortal.setProcessorEnabled(tfod, true);

    }   // end method initTfod()

    /**
     * Add telemetry about TensorFlow Object Detection (TFOD) recognitions.
     */
    private void telemetryTfod() {

        List<Recognition> currentRecognitions = tfod.getRecognitions();
        telemetry.addData("# Objects Detected", currentRecognitions.size());
        
        if(currentRecognitions.size()>0){hasTargets = true;}
        else {hasTargets = false;}

        // Step through the list of recognitions and display info for each one.
        for (Recognition recognition : currentRecognitions) {
            targetX = (recognition.getLeft() + recognition.getRight()) / 2 ;
            double y = (recognition.getTop()  + recognition.getBottom()) / 2 ;

            telemetry.addData(""," ");
            telemetry.addData("Image", "%s (%.0f %% Conf.)", recognition.getLabel(), recognition.getConfidence() * 100);
            telemetry.addData("- Position", "%.0f / %.0f", targetX, y);
            telemetry.addData("- Size", "%.0f x %.0f", recognition.getWidth(), recognition.getHeight());
        }   // end for() loop

    }   // end method telemetryTfod()
    
    // Method for driving with encoder
    public void encoderDrive(double speed,
                             double leftInches, double rightInches, double backleftInches, double backrightInches,
                             double timeoutS) {
        leftInches*=-1;
        rightInches*=-1;
        backleftInches*=-1;
        backrightInches*=-1;
        int newLeftTarget;
        int newRightTarget;
        int newBackLeftTarget;
        int newBackRightTarget;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {

            // Determine new target position, and pass to motor controller
            newLeftTarget = motorFrontLeft.getCurrentPosition() + (int) (leftInches * COUNTS_PER_INCH);
            newRightTarget = motorFrontRight.getCurrentPosition() + (int) (rightInches * COUNTS_PER_INCH);
            newBackLeftTarget = motorBackLeft.getCurrentPosition() + (int) (backleftInches * COUNTS_PER_INCH);
            newBackRightTarget = motorBackRight.getCurrentPosition() + (int) (backrightInches * COUNTS_PER_INCH);
            motorFrontLeft.setTargetPosition(newLeftTarget);
            motorFrontRight.setTargetPosition(newRightTarget);
            motorBackLeft.setTargetPosition(newBackLeftTarget);
            motorBackRight.setTargetPosition(newBackRightTarget);

            // Turn On RUN_TO_POSITION
            motorFrontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motorFrontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motorBackLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motorBackRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            motorBackLeft.setPower(Math.abs(speed));
            motorBackRight.setPower(Math.abs(speed));
            motorFrontLeft.setPower(Math.abs(speed));
            motorFrontRight.setPower(Math.abs(speed));

            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS) &&
                    (motorBackLeft.isBusy() && motorBackRight.isBusy())) {

                // Display it for the driver.
                telemetry.addData("Path1", "Running to %7d :%7d", newLeftTarget, newRightTarget, newBackLeftTarget,
                        newBackRightTarget);
                telemetry.addData("Path2", "Running at %7d :%7d",
                        motorFrontLeft.getCurrentPosition(),
                        motorFrontRight.getCurrentPosition(),
                        motorBackLeft.getCurrentPosition(),
                        motorBackRight.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            motorBackLeft.setPower(0);
            motorBackRight.setPower(0);
            motorFrontLeft.setPower(0);
            motorFrontRight.setPower(0);

            // Turn off RUN_TO_POSITION
            motorFrontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorFrontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorBackLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            motorBackRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        }

    }

    // This method reads the IMU getting the angle. It automatically adjusts the
    // angle so that it is between -180 and +180.
    public double getAngle() {
        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        double deltaAngle = angles.firstAngle - lastAngles.firstAngle;

        if (deltaAngle < -180)
            deltaAngle += 360;
        else if (deltaAngle > 180)
            deltaAngle -= 360;

        globalAngle += deltaAngle;

        lastAngles = angles;

        return globalAngle;
    }

    // The method turns the robot by a specific angle, -180 to +180.
    public void rotate(int degrees, double power) {
        double backLeftPower, backRightPower, frontLeftPower, frontRightPower;

        resetAngle();

        // if the degrees are less than 0, the robot will turn right
        if (degrees < 0) {
            motorBackLeft.setPower(power);
            motorFrontLeft.setPower(power);
            motorBackRight.setPower(-power);
            motorFrontRight.setPower(-power);
        } else if (degrees > 0)// if greater than 0, turn left
        {
            motorBackLeft.setPower(-power);
            motorFrontLeft.setPower(-power);
            motorBackRight.setPower(power);
            motorFrontRight.setPower(power);
        } else
            return;

        // Repeatedly check the IMU until the getAngle() function returns the value
        // specified.
        if (degrees < 0) {
            while (opModeIsActive() && getAngle() == 0) {
            }

            while (opModeIsActive() && getAngle() > degrees) {
            }
        } else
            while (opModeIsActive() && getAngle() < degrees) {
            }

        // stop the motors after the angle has been found.

        motorBackLeft.setPower(0);
        motorBackRight.setPower(0);
        motorFrontLeft.setPower(0);
        motorFrontRight.setPower(0);

        // sleep for a bit to make sure the robot doesn't over shoot
        sleep(1000);

        resetAngle();
    }

    // this method resets the angle so that the robot's heading is now 0
    public void resetAngle() {
        lastAngles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        globalAngle = 0;
    }
    
    //public void servo to a postion

   public void turnServo(double servoPosition) {
        //autonServo.setPosition(Range.clip(servoPosition, MIN_POSITION, MAX_POSITION));
        autonServo.setPosition(servoPosition);
        sleep(1000);
        autonServo.setPosition(servoPosition * -1);
    }   
}