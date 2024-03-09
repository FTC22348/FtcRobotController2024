/* Base code from:
https://gm0.org/en/latest/docs/software/tutorials/mecanum-drive.html

Main TeleOP (2 Driver)

Ports:
Motors:
- Motor Front Left - Main 1
- Motor Back Left - Main 2
- Motor Front Right - Expansion 1
- Motor Back Right - Expansion 2
- Spinner - Control 0
- Worm gear - Expansion 0 (Right Side)
- Muscle - Main 3 (Left Side)
- Spool - Expansion 3

Servos: 
- Rack and Pinion - Expansion 5
- Bucket - Expansion 2
- Servo arm - Expansion 3
- Drone - Expansion 5
- Intake - Main 1
- Ramp - Main 2
- Hinge - Main 4
*/

package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "TeleOP")

public class Reapr_Main_TeleOP extends LinearOpMode {
    double hingePosition;
    double  MIN_POSITION = 0, MAX_POSITION = 1;
    @Override
    public void runOpMode() throws InterruptedException {
        // Declare our motors
        // Make sure your ID's match your configuration

        // Meccanum Drivetrain
        DcMotor motorFrontLeft = hardwareMap.dcMotor.get("motorFrontLeft");
        DcMotor motorBackLeft = hardwareMap.dcMotor.get("motorBackLeft");
        DcMotor motorFrontRight = hardwareMap.dcMotor.get("motorFrontRight");
        DcMotor motorBackRight = hardwareMap.dcMotor.get("motorBackRight");

        // Reverse the right side motors
        // Reverse left motors if you are using NeveRests
        motorFrontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        motorBackLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        // motorBackRight.setDirection(DcMotorSimple.Direction.REVERSE);  // This was connected on the expansion hub, it needs to be reversed

        DcMotor wormGear = hardwareMap.dcMotor.get("wormGear"); // Port 0
        DcMotor muscle = hardwareMap.dcMotor.get("muscle"); // Port 3
        // DcMotor spinner = hardwareMap.dcMotor.get("spinnerMotor"); // Port 0 (intake is a servo now)
        CRServo intake = hardwareMap.crservo.get("intake");
        CRServo ramp = hardwareMap.crservo.get("ramp");
        Servo hinge = hardwareMap.servo.get("hinge");
        DcMotor spool = hardwareMap.dcMotor.get("spool"); // Port 3

        // Airplane launcher setup
        Servo launcher = hardwareMap.servo.get("launcher");
        CRServo bucketArm = hardwareMap.crservo.get("bucketArm");
        CRServo bucket = hardwareMap.crservo.get("bucket");


        /*
        double launcherPosition = 0.5;
        final double launcherSpeed = 0.1;// change to 100th when button is hold
        final double launcherMinRange = 0.3;
        final double launcherMaxRange = 0.55;
        */

        // Rack and pinion setup


        waitForStart();

        hingePosition = 0.1;

        if (isStopRequested()) return;

        boolean isSlowMode = false;
        double dividePower=1.0;

        int currentPosition = 0;

        while (opModeIsActive()) {
            //! Control Speed
            if(isSlowMode){
                dividePower=1.5;
            }else{
                dividePower=1.0;
            }

            if(gamepad1.left_stick_button){
                if(isSlowMode){
                    isSlowMode=false;
                    sleep(500);
                }else{
                    isSlowMode=true;
                    sleep(500);
                }
            }

            //! Mecccanum controls
            double y = -gamepad1.left_stick_y; // Remember, this is reversed!
            double x = gamepad1.left_stick_x * 1.1; // Counteract imperfect strafing
            double rx = gamepad1.right_stick_x;


            // Denominator is the largest motor power (absolute value) or 1
            // This ensures all the powers maintain the same ratio, but only when
            // at least one is out of the range [-1, 1]
            double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
            double frontLeftPower = (y + x + rx) / denominator / dividePower; //Positive rotation results in forward & right motion
            double backLeftPower = (y - x + rx) / denominator / dividePower; //Positive rotation results in forward & left motion
            double frontRightPower = (y - x - rx) / denominator / dividePower; //Positive rotation results in forward & left motion
            double backRightPower = (y + x - rx) / denominator / dividePower; //Positive rotation results in forward & right motion

            motorFrontLeft.setPower(frontLeftPower);
            motorBackLeft.setPower(backLeftPower);
            motorFrontRight.setPower(frontRightPower);
            motorBackRight.setPower(backRightPower);


            //! Misc motor controls
            // Worm Gear motor Controls
            // For hanging arm
            if (gamepad2.a){ // Move down
                wormGear.setPower(1);
                muscle.setPower(1);
            }
            wormGear.setPower(0);
            muscle.setPower(0);

            if (gamepad2.y){ // Move up
                wormGear.setPower(-1);
                muscle.setPower(-1);
            }
            wormGear.setPower(0);
            muscle.setPower(0);


            // Spinner (intake) motor
            if (gamepad1.a){ // Move down
                intake.setPower(1);
                ramp.setPower(-1);
            }else if (gamepad1.y){ // Move up
                intake.setPower(-1);
                ramp.setPower(1);
            }else{
                intake.setPower(0);
                ramp.setPower(0);
            }




            //! Servo Controls

            // Drone Launcher

            //telemetry.addData("Drone launcher", "%.2f", launcherPosition); //displays the values on the driver hub
            //telemetry.update();
            if (gamepad1.b) {
                launcher.setPosition(-1);
                //telemetry.update();
            }
            else if (gamepad1.x){
                launcher.setPosition(1);
                //telemetry.update();
            }


            // Bucket arm
            if (gamepad2.dpad_down) { // Go down
                bucketArm.setPower(-0.75);
            }else if (gamepad2.dpad_up){ // Go up
                bucketArm.setPower(0.75);
            }else{
                bucketArm.setPower(0);
            }


            // Bucket
            if(gamepad2.b){
                bucket.setPower(1);
                //telemetry.update();
            }else if(gamepad2.x){
                bucket.setPower(-1);
                //telemetry.update();
            }else{
                bucket.setPower(0);

            }

            // move hinge down on dpad left button if not already at lowest position.
            if (gamepad2.dpad_left && hingePosition > MIN_POSITION) hingePosition -= .01;

            // move hinge up on dpad right button if not already at the highest position.
            if (gamepad2.dpad_right && hingePosition < MAX_POSITION) hingePosition += .01;

            hinge.setPosition(Range.clip(hingePosition, MIN_POSITION, MAX_POSITION));
            telemetry.addData("hinge servo", "position=" + hingePosition + "  actual=" + hinge.getPosition());
            telemetry.update();

            // Spool
            if (gamepad2.dpad_left){
                spool.setPower(1);
            }
            spool.setPower(0);
            if (gamepad2.dpad_right) {
                spool.setPower(-1);
            } spool.setPower(0);
        }
    }
}
