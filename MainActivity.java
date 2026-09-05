package com.example.bimeinstallments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String PREFS = "insurance_data";
    private static final String KEY_POLICIES = "policies";
    private final List<Policy> policies = new ArrayList<>();
    private SharedPreferences prefs;
    private LinearLayout content;
    private final NumberFormat money = NumberFormat.getInstance(new Locale("fa", "IR"));

    private static class Policy {
        String customer = "";
        String phone = "";
        String plate = "";
        String policyNo = "";
        long total;
        long down;
        long paid;
        int installments;
        String firstDue = "";
        String note = "";

        long debt() { return Math.max(0L, total - paid); }
        long each() { return installments > 0 ? (total - down) / installments : 0; }
        int paidCount() { long e = each(); return e > 0 ? (int)Math.min(installments, Math.max(0, (paid - down) / e)) : 0; }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        loadData();
        buildHome();
    }

    private int dp(int value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }

    private TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(Color.rgb(40,40,40));
        t.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        t.setPadding(dp(14), dp(10), dp(14), dp(10));
        return t;
    }

    private Button menuButton(String value) {
        Button b = new Button(this);
        b.setText(value); b.setTextSize(16); b.setAllCaps(false); b.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(58));
        p.setMargins(dp(5), dp(5), dp(5), dp(5)); b.setLayoutParams(p);
        return b;
    }

    private EditText input(String hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint); e.setTextSize(16); e.setGravity(Gravity.RIGHT); e.setInputType(type);
        e.setPadding(dp(12), 0, dp(12), 0);
        e.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(54)));
        return e;
    }

    private void base(String title) {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackgroundColor(Color.rgb(246,248,249));
        TextView header = text(title, 21);
        header.setTextColor(Color.WHITE); header.setBackgroundColor(Color.rgb(0,108,82)); header.setGravity(Gravity.CENTER);
        root.addView(header, new LinearLayout.LayoutParams(-1, dp(72)));
        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        content.setPadding(dp(10), dp(10), dp(10), dp(20)); scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }

    private TextView stat(String title, String value) {
        TextView t = text(title + "\n" + value, 15); t.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(82),1); p.setMargins(dp(4),0,dp(4),dp(10));
        t.setLayoutParams(p); return t;
    }

    private void buildHome() {
        base("مدیریت حرفه‌ای اقساط بیمه ثالث");
        long total=0, paid=0, debt=0;
        for (Policy p : policies) { total += p.total; paid += p.paid; debt += p.debt(); }
        LinearLayout stats = new LinearLayout(this); stats.setOrientation(LinearLayout.HORIZONTAL); stats.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        stats.addView(stat("بیمه‌نامه", String.valueOf(policies.size())));
        stats.addView(stat("دریافتی", money.format(paid)));
        stats.addView(stat("مطالبات", money.format(debt)));
        content.addView(stats);

        Button add = menuButton("＋ ثبت بیمه‌نامه جدید"); add.setOnClickListener(v -> showAddPolicy()); content.addView(add);
        Button list = menuButton("👥 مشتریان و بیمه‌نامه‌ها"); list.setOnClickListener(v -> showPolicies()); content.addView(list);
        Button inst = menuButton("▣ اقساط و سررسیدها"); inst.setOnClickListener(v -> showInstallments()); content.addView(inst);
        Button pay = menuButton("✓ ثبت پرداخت"); pay.setOnClickListener(v -> showPayment()); content.addView(pay);
        Button reports = menuButton("▤ گزارش مالی"); reports.setOnClickListener(v -> showReports()); content.addView(reports);
        Button search = menuButton("⌕ جستجوی مشتری / پلاک / بیمه‌نامه"); search.setOnClickListener(v -> showSearch()); content.addView(search);
        Button backup = menuButton("↥ پشتیبان‌گیری اطلاعات"); backup.setOnClickListener(v -> showBackup()); content.addView(backup);
        content.addView(text("اطلاعات نسخه ۲ روی دستگاه ذخیره می‌شود و بعداً می‌توان آن را به دیتابیس ابری متصل کرد.",13));
    }

    private void showAddPolicy() {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(6),0,dp(6),0); box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        EditText customer=input("نام و نام خانوادگی مشتری *",InputType.TYPE_CLASS_TEXT); EditText phone=input("شماره موبایل",InputType.TYPE_CLASS_PHONE);
        EditText plate=input("پلاک خودرو",InputType.TYPE_CLASS_TEXT); EditText policyNo=input("شماره بیمه‌نامه",InputType.TYPE_CLASS_TEXT);
        EditText total=input("مبلغ کل (تومان) *",InputType.TYPE_CLASS_NUMBER); EditText down=input("پیش‌پرداخت (تومان) *",InputType.TYPE_CLASS_NUMBER);
        EditText count=input("تعداد اقساط *",InputType.TYPE_CLASS_NUMBER); EditText due=input("اولین سررسید (مثلاً 1405/07/15)",InputType.TYPE_CLASS_TEXT);
        EditText note=input("توضیحات",InputType.TYPE_CLASS_TEXT);
        box.addView(customer); box.addView(phone); box.addView(plate); box.addView(policyNo); box.addView(total); box.addView(down); box.addView(count); box.addView(due); box.addView(note);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("ثبت بیمه‌نامه").setView(box).setNegativeButton("انصراف",null).setPositiveButton("ثبت",null).create();
        dialog.setOnShowListener(x -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                String c=customer.getText().toString().trim(); long t=Long.parseLong(total.getText().toString().trim()); long d=Long.parseLong(down.getText().toString().trim()); int n=Integer.parseInt(count.getText().toString().trim());
                if(c.isEmpty()||t<=0||d<0||d>t||n<=0) throw new Exception();
                Policy p=new Policy(); p.customer=c; p.phone=phone.getText().toString().trim(); p.plate=plate.getText().toString().trim(); p.policyNo=policyNo.getText().toString().trim(); p.total=t; p.down=d; p.paid=d; p.installments=n; p.firstDue=due.getText().toString().trim(); p.note=note.getText().toString().trim(); policies.add(p); saveData(); dialog.dismiss(); buildHome();
            } catch(Exception e) { total.setError("اطلاعات مبلغ، پیش‌پرداخت و اقساط صحیح نیست"); }
        }));
        dialog.show();
    }

    private void showPolicies() {
        base("مشتریان و بیمه‌نامه‌ها");
        if(policies.isEmpty()){content.addView(text("هنوز رکوردی ثبت نشده است.",18)); return;}
        for(Policy p:policies){
            TextView item=text("مشتری: "+p.customer+"\nپلاک: "+p.plate+" | بیمه‌نامه: "+p.policyNo+"\nکل: "+money.format(p.total)+" | پرداخت: "+money.format(p.paid)+"\nمانده: "+money.format(p.debt()),15);
            item.setBackgroundColor(Color.WHITE); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(124)); lp.setMargins(0,0,0,dp(10)); content.addView(item,lp);
        }
    }

    private void showInstallments() {
        base("اقساط و سررسیدها");
        if(policies.isEmpty()){content.addView(text("اطلاعاتی برای نمایش وجود ندارد.",18)); return;}
        for(Policy p:policies){
            long each=p.each(); int paidCount=p.paidCount();
            content.addView(text(p.customer+" - "+p.plate+"\nهر قسط: "+money.format(each)+" تومان | اولین سررسید: "+(p.firstDue.isEmpty()?"ثبت نشده":p.firstDue),16));
            for(int i=1;i<=p.installments;i++){
                String state=i<=paidCount?"✓ پرداخت شده":"○ در انتظار پرداخت";
                TextView item=text("قسط "+i+" از "+p.installments+" | "+state+"\nمبلغ: "+money.format(each)+" تومان",15); item.setBackgroundColor(Color.WHITE);
                content.addView(item,new LinearLayout.LayoutParams(-1,dp(78)));
            }
            content.addView(text("مانده: "+money.format(p.debt())+" تومان",16));
        }
    }

    private void showPayment() {
        if(policies.isEmpty()){new AlertDialog.Builder(this).setTitle("ثبت پرداخت").setMessage("ابتدا یک بیمه‌نامه ثبت کنید.").setPositiveButton("باشه",null).show(); return;}
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); box.setPadding(dp(6),0,dp(6),0);
        String[] names=new String[policies.size()]; for(int i=0;i<policies.size();i++) names[i]=policies.get(i).customer+" - "+policies.get(i).plate;
        Spinner spinner=new Spinner(this); spinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names)); box.addView(spinner);
        EditText amount=input("مبلغ پرداختی *",InputType.TYPE_CLASS_NUMBER); box.addView(amount); EditText note=input("توضیحات پرداخت",InputType.TYPE_CLASS_TEXT); box.addView(note);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("ثبت پرداخت").setView(box).setNegativeButton("انصراف",null).setPositiveButton("ثبت",null).create();
        dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{try{long a=Long.parseLong(amount.getText().toString().trim()); if(a<=0)throw new Exception(); Policy p=policies.get(spinner.getSelectedItemPosition()); p.paid=Math.min(p.total,p.paid+a); saveData(); dialog.dismiss(); buildHome();}catch(Exception e){amount.setError("مبلغ نامعتبر است");}})); dialog.show();
    }

    private void showReports(){
        base("گزارش مالی"); long total=0,paid=0,debt=0; int count=0;
        for(Policy p:policies){total+=p.total;paid+=p.paid;debt+=p.debt();count++;}
        content.addView(stat("تعداد بیمه‌نامه",String.valueOf(count))); content.addView(stat("مجموع فروش",money.format(total)+" تومان")); content.addView(stat("مجموع دریافتی",money.format(paid)+" تومان")); content.addView(stat("مجموع مطالبات",money.format(debt)+" تومان"));
        content.addView(text("درصد وصولی: "+(total==0?0:(paid*100/total))+"٪",18));
    }

    private void showSearch(){
        final EditText q=input("نام، پلاک یا شماره بیمه‌نامه",InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(this).setTitle("جستجو").setView(q).setNegativeButton("لغو",null).setPositiveButton("جستجو",(d,w)->{base("نتیجه جستجو"); String s=q.getText().toString().trim(); boolean found=false; for(Policy p:policies){if(p.customer.contains(s)||p.plate.contains(s)||p.policyNo.contains(s)){found=true;content.addView(text(p.customer+" | "+p.plate+"\nبیمه‌نامه: "+p.policyNo+"\nمانده: "+money.format(p.debt()),16));}} if(!found)content.addView(text("موردی پیدا نشد.",18));}).show();
    }

    private void showBackup(){
        StringBuilder sb=new StringBuilder("تعداد رکورد: ").append(policies.size()).append("\n\n"); for(Policy p:policies) sb.append(p.customer).append(" | ").append(p.plate).append(" | مانده: ").append(money.format(p.debt())).append(" تومان\n");
        new AlertDialog.Builder(this).setTitle("خلاصه پشتیبان").setMessage(sb.toString()).setPositiveButton("باشه",null).show();
    }

    private void saveData(){
        try{JSONArray arr=new JSONArray(); for(Policy p:policies){JSONObject o=new JSONObject();o.put("customer",p.customer);o.put("phone",p.phone);o.put("plate",p.plate);o.put("policyNo",p.policyNo);o.put("total",p.total);o.put("down",p.down);o.put("paid",p.paid);o.put("installments",p.installments);o.put("firstDue",p.firstDue);o.put("note",p.note);arr.put(o);} prefs.edit().putString(KEY_POLICIES,arr.toString()).apply();}catch(Exception ignored){}
    }

    private void loadData(){
        policies.clear(); String raw=prefs.getString(KEY_POLICIES,"[]"); try{JSONArray arr=new JSONArray(raw); for(int i=0;i<arr.length();i++){JSONObject o=arr.getJSONObject(i);Policy p=new Policy();p.customer=o.optString("customer");p.phone=o.optString("phone");p.plate=o.optString("plate");p.policyNo=o.optString("policyNo");p.total=o.optLong("total");p.down=o.optLong("down");p.paid=o.optLong("paid");p.installments=o.optInt("installments");p.firstDue=o.optString("firstDue");p.note=o.optString("note");policies.add(p);}}catch(Exception ignored){}
    }
}
