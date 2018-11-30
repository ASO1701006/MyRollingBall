package jp.ac.asojuku.st.myrollingball

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),SensorEventListener,SurfaceHolder.Callback {

    //プロパティ
    private var surfaceWidth:Int = 0;//サーフェスの幅
    private var surfaceHeight:Int = 0;//サーフェスの高さ

    private val radius = 50.0f;//ボールの半径
    private var coef = 0.0f;//ボールの移動量を計算するための係数

    private var ballX:Float = 0f;//ボールの現在のx座標
    private var ballY:Float = 0f;//ボールの現在のy座標
    private var vx:Float = 0f;//ボールのx方向への加速度
    private var vy:Float = 0f;//ボールのy方向への加速度
    private var time:Long = 0L;//前回時間の保持


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val holder =surfaceView.holder
        holder.addCallback(this)

         ResetButton.setOnClickListener{
             val width = surfaceWidth
             val height = surfaceHeight
            ballX = (width/2).toFloat();
            ballY = (height/10).toFloat()
             //commentreset
             commentreset()
             //coef = 500.0f
             coef = 0f
             vx = 0f
             vy = 0f
             imageView.setImageResource(R.drawable.family_01_pc)
      }

        StartButton.setOnClickListener{
            val width = surfaceWidth
            val height = surfaceHeight
            vx = 0f
            vy = 0f

            if(coef == 0.0f && ballX == (width/2).toFloat() && ballY == (height/10).toFloat()){
                coef = 500f
            }
        }

    }

    override fun onResume() {
        super.onResume()

    }




    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null){return}
        //ボールの描画の計算処理
        if(time==0L) {
            time = System.currentTimeMillis();//最初のタイミングでは現在時刻を保存
        }
            //イベントのセンサー識別の情報がアクセラメーター（加速度センサー）の時だけ以下の処理を実行
            if(event.sensor.type == Sensor.TYPE_ACCELEROMETER){
                val x = event.values[0]*-1;
                val y = event.values[1];

                //経過時間を計算（今の時間ー前の時間＝経過時間）
                var t = (System.currentTimeMillis()-time).toFloat();
                //今の時間を前の時間として保存
                time = System.currentTimeMillis();
                t/=1000.0f;

                //移動距離を計算（ボールをどれくらいウドかすか）
                val dx = (vx*t) + (x*t*t)/2.0f;
                val dy = (vy*t) + (x*t*t)/2.0f;
                ballY += (dy*coef);
                ballX += (dx*coef);
                vx +=(x*t);
                vy +=(y*t);

                //画面の端に来たら跳ね返る処理
                //左右について
                if((ballX - radius)<0 && vx<0){
                    //左
                    vx = -vx/1.5f;
                    ballX = radius;
                }else if ((ballX+radius)>surfaceWidth && vx>0){
                    //右
                    vx = -vx/1.5f;
                    ballX = (surfaceWidth-radius);
                }
                //上下について
                if ((ballY-radius)<0 && vy<0){
                    //下
                    vy = -vy/1.5f;
                    ballY = radius;
                }else if((ballY+radius)>surfaceHeight && vy>0){
                    //上
                    vy = -vy/1.5f;
                    ballY = surfaceHeight-radius;
                }
                //障害物への処理
                if(ballX + radius>300f && ballX - radius<500f && ballY + radius>100f && ballY - radius<300f
                ||ballX + radius>1100f && ballX - radius<1500f && ballY + radius>300f && ballY - radius<600f
                ||ballX + radius>0f && ballX - radius<600f && ballY + radius>700f && ballY - radius<1000f
                ||ballX + radius>1000f && ballX - radius<1500f && ballY + radius>1000f && ballY - radius<1100f){
                    coef = 0f;
                    gameover()
                }

                //GOAL判定の処理
                if(ballX + radius>0f && ballX - radius<50f && ballY + radius>1400f && ballY - radius<1500f){
                    coef = 0f
                    goal()
                }


                drawCanas();

            }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }



    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, width: Int, height: Int) {
        surfaceWidth = width;
        surfaceHeight = height;

        //ボール位置の初期設定
        ballX = (width/2).toFloat();
        ballY = (height/10).toFloat();


    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        sensorManager.unregisterListener(this);

    }

    override fun surfaceCreated(p0: SurfaceHolder?) {
        val sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager;
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(
                this,
                accSensor,
                SensorManager.SENSOR_DELAY_GAME)
    }

    //サーフェスのキャンバスに描画する
    private fun drawCanas(){
        val canvas = surfaceView.holder.lockCanvas();
        canvas.drawColor(Color.BLACK);
        canvas.drawCircle(ballX, ballY, radius, Paint().apply {
            color = Color.RED
        })
        canvas.drawRect(300f,100f,500f,300f,Paint().apply {
            color = Color.BLUE
        })
        canvas.drawRect(1100f,300f,1500f,600f,Paint().apply {
            color = Color.BLUE
        })
        canvas.drawRect(0f,700f,600f,1000f,Paint().apply {
            color = Color.BLUE
        })
        canvas.drawRect(1000f,1000f,1500f,1100f,Paint().apply {
            color = Color.BLUE
        })
        //goal
        canvas.drawRect(0f,1400f,50f,1500f,Paint().apply {
            color = Color.GREEN
        })
        surfaceView.holder.unlockCanvasAndPost(canvas)
    }

    private fun gameover(){
        commentid.text = "GAME OVER"
        imageView.setImageResource(R.drawable.banana)

    }
    private fun commentreset(){
        commentid.text = "頑張れ"

    }
    private fun goal(){
        commentid.text = "君は天才だ"
    }



}
