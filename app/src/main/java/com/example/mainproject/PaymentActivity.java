public class PaymentActivity extends AppCompatActivity {

    TextView tvPlanName;
    EditText etName, etReference, etAmount;
    Button btnPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        tvPlanName = findViewById(R.id.tvPlanName);
        etName = findViewById(R.id.etName);
        etReference = findViewById(R.id.etReference);
        etAmount = findViewById(R.id.etAmount);
        btnPay = findViewById(R.id.btnPay);

        // Get plan details from intent
        Intent intent = getIntent();
        String planName = intent.getStringExtra("planName");
        String amount = intent.getStringExtra("amount");

        tvPlanName.setText("Plan: " + planName);
        etAmount.setText(amount);

        btnPay.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String reference = etReference.getText().toString().trim();

            if (name.isEmpty() || reference.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Proceed with payment logic
                Toast.makeText(this, "Payment Submitted", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
