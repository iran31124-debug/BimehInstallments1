package ir.bimeh.installments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends android.app.Activity {
    private DB db;
    private LinearLayout body;
    private String currentPage = "خانه";
    private final NumberFormat money = NumberFormat.getInstance(new Locale("fa", "IR"));
    private final int GREEN = Color.rgb(11,107,83);
    private final int DARK = Color.rgb(6,76,60);
    private final int BG = Color.rgb(244,247,245);
    private final int TEXT = Color.rgb(25,35,31);
    private final int MUTED = Color.rgb(98,113,106);
    private final int RED = Color.rgb(183,45,39);
    private final int ORANGE = Color.rgb(176,116,10);
    private final int WHITE = Color.WHITE;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        db = new DB(this);
        render("خانه");
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private GradientDrawable bg(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color); g.setCornerRadius(dp(radius));
        return g;
    }

    private TextView text(String s, float size, int color) {
        TextView t = new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        t.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        t.setPadding(dp(12), dp(7), dp(12), dp(7));
        return t;
    }

    private Button actionButton(String s) {
        Button b = new Button(this);
        b.setText(s); b.setTextSize(15); b.setTextColor(WHITE); b.setAllCaps(false);
        b.setGravity(Gravity.CENTER); b.setBackground(bg(GREEN, 18));
        b.setPadding(dp(8), dp(2), dp(8), dp(2));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(54));
        p.setMargins(0, dp(6), 0, dp(6)); b.setLayoutParams(p);
        return b;
    }

    private EditText input(String hint, int type) {
        EditText e = new EditText(this);
        e.setHint(hint); e.setTextSize(16); e.setTextColor(TEXT); e.setHintTextColor(MUTED);
        e.setGravity(Gravity.RIGHT); e.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        e.setInputType(type); e.setSingleLine(true); e.setPadding(dp(14), 0, dp(14), 0);
        e.setBackground(bg(WHITE, 16));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(52));
        p.setMargins(0, dp(5), 0, dp(5)); e.setLayoutParams(p);
        return e;
    }

    private TextView chip(String label, int color) {
        TextView t = text(label, 13, color); t.setGravity(Gravity.CENTER); t.setTypeface(null, Typeface.BOLD);
        t.setBackground(bg(Color.argb(28, Color.red(color), Color.green(color), Color.blue(color)), 20));
        t.setPadding(dp(10),0,dp(10),0);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, dp(34)); p.setMargins(dp(5),0,dp(5),0);
        t.setLayoutParams(p); return t;
    }

    private TextView statCard(String title, String value) {
        TextView t = text(title + "\n" + value, 16, TEXT);
        t.setTypeface(null, Typeface.BOLD); t.setBackground(bg(WHITE, 18));
        t.setPadding(dp(14), dp(10), dp(14), dp(10));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(92), 1);
        p.setMargins(dp(4), 0, dp(4), dp(10)); t.setLayoutParams(p); return t;
    }

    private void render(String page) {
        currentPage = page;
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG); root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        LinearLayout top = new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(dp(16),0,dp(8),0); top.setBackgroundColor(DARK);
        TextView title = text(page, 20, WHITE); title.setTypeface(null, Typeface.BOLD); top.addView(title, new LinearLayout.LayoutParams(0, dp(68), 1));
        TextView menu = text("⋮", 28, WHITE); menu.setGravity(Gravity.CENTER); menu.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ settings(); }});
        top.addView(menu, new LinearLayout.LayoutParams(dp(50), dp(68))); root.addView(top);

        FrameLayout center = new FrameLayout(this);
        ScrollView sv = new ScrollView(this); body = new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(14),dp(14),dp(14),dp(26)); body.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); sv.addView(body); center.addView(sv); root.addView(center, new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout nav = new LinearLayout(this); nav.setOrientation(LinearLayout.HORIZONTAL); nav.setBackgroundColor(WHITE); nav.setPadding(dp(3),dp(4),dp(3),dp(6));
        String[] pages = {"خانه","پرونده‌ها","اقساط","گزارش","مشتریان"};
        for (final String p : pages) {
            Button n = new Button(this); n.setText(p); n.setAllCaps(false); n.setTextSize(12); n.setTextColor(p.equals(page)?GREEN:MUTED); n.setBackgroundColor(Color.TRANSPARENT);
            n.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ render(p); }});
            nav.addView(n, new LinearLayout.LayoutParams(0,dp(58),1));
        }
        root.addView(nav); setContentView(root);

        if (page.equals("خانه")) home(); else if(page.equals("پرونده‌ها")) policies(); else if(page.equals("اقساط")) installments(); else if(page.equals("گزارش")) reports(); else customers();
    }

    private void home() {
        body.addView(text("مدیریت حرفه‌ای اقساط بیمه ثالث",24,TEXT));
        body.addView(text("نسخه ۲۰۲۶ • کنترل پرونده، اقساط و دریافتی‌ها",14,MUTED));
        LinearLayout stats = row();
        long total=db.sum("total"), paid=db.sum("paid"), debt=Math.max(0,total-paid);
        stats.addView(statCard("پرونده فعال",String.valueOf(db.count("policies"))));
        stats.addView(statCard("دریافتی",money.format(paid)+" تومان"));
        stats.addView(statCard("مطالبات",money.format(debt)+" تومان"));
        stats.addView(statCard("اقساط مانده",String.valueOf(db.remainingInstallments()))); body.addView(stats);

        Button add=actionButton("＋  ثبت بیمه‌نامه جدید"); add.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ policyDialog(null); }}); body.addView(add);
        Button pay=actionButton("✓  ثبت پرداخت قسط"); pay.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ paymentDialog(); }}); body.addView(pay);
        Button due=actionButton("⏰  سررسیدها و اقساط معوق"); due.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ render("اقساط"); }}); body.addView(due);
        Button search=actionButton("🔎  جستجوی مشتری / پلاک / بیمه‌نامه"); search.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ searchDialog(); }}); body.addView(search);

        body.addView(text("آخرین پرونده‌ها",18,TEXT));
        CursorWrap c=new CursorWrap(db.query("SELECT id,customer,phone,plate,policyNo,total,paid FROM policies ORDER BY id DESC LIMIT 5"));
        while(c.next()) addPolicyCard(c.id,c.customer,c.phone,c.plate,c.policyNo,c.total,c.paid,true);
        c.close();
    }

    private LinearLayout row(){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return l; }

    private void policies(){
        Button add=actionButton("＋ ثبت بیمه‌نامه جدید"); add.setOnClickListener(new View.OnClickListener(){public void onClick(View v){policyDialog(null);}}); body.addView(add);
        Button search=actionButton("🔎 جستجو در پرونده‌ها"); search.setOnClickListener(new View.OnClickListener(){public void onClick(View v){searchDialog();}}); body.addView(search);
        CursorWrap c=new CursorWrap(db.query("SELECT id,customer,phone,plate,policyNo,total,paid FROM policies ORDER BY id DESC"));
        if(!c.next()){ body.addView(text("هنوز پرونده‌ای ثبت نشده است.",18,MUTED)); c.close(); return; }
        do { addPolicyCard(c.id,c.customer,c.phone,c.plate,c.policyNo,c.total,c.paid,false); } while(c.next()); c.close();
    }

    private void addPolicyCard(final long id,String customer,String phone,String plate,String policyNo,long total,long paid,boolean compact){
        final LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(14),dp(12),dp(14),dp(12)); box.setBackground(bg(WHITE,18));
        TextView h=text(customer,18,TEXT); h.setTypeface(null,Typeface.BOLD); box.addView(h);
        box.addView(text("بیمه‌نامه: "+policyNo+"  •  پلاک: "+plate+"\nموبایل: "+phone,14,MUTED));
        long debt=Math.max(0,total-paid); int remain=db.remainingInstallmentsForPolicy(id);
        LinearLayout chips=row(); chips.addView(chip("کل: "+money.format(total),GREEN)); chips.addView(chip("مانده: "+money.format(debt),debt>0?RED:GREEN)); chips.addView(chip("قسط مانده: "+remain, remain>0?ORANGE:GREEN)); box.addView(chips);
        if(!compact){
            LinearLayout actions=row();
            Button detail=new Button(this); detail.setText("جزئیات"); detail.setAllCaps(false); detail.setTextColor(GREEN); detail.setBackgroundColor(Color.TRANSPARENT); detail.setOnClickListener(new View.OnClickListener(){public void onClick(View v){ detailDialog(id); }});
            Button edit=new Button(this); edit.setText("ویرایش"); edit.setAllCaps(false); edit.setTextColor(ORANGE); edit.setBackgroundColor(Color.TRANSPARENT); edit.setOnClickListener(new View.OnClickListener(){public void onClick(View v){ policyDialog(id); }});
            Button del=new Button(this); del.setText("حذف"); del.setAllCaps(false); del.setTextColor(RED); del.setBackgroundColor(Color.TRANSPARENT); del.setOnClickListener(new View.OnClickListener(){public void onClick(View v){ confirmDelete(id); }});
            actions.addView(detail,new LinearLayout.LayoutParams(0,dp(52),1)); actions.addView(edit,new LinearLayout.LayoutParams(0,dp(52),1)); actions.addView(del,new LinearLayout.LayoutParams(0,dp(52),1)); box.addView(actions);
        }
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,compact?dp(132):dp(188)); bp.setMargins(0,0,0,dp(10)); body.addView(box,bp);
    }

    private void detailDialog(long id){
        CursorWrap c=new CursorWrap(db.query("SELECT customer,phone,plate,policyNo,total,down,paid,firstDue FROM policies WHERE id="+id));
        if(!c.next()){c.close();return;}
        String s="مشتری: "+c.customer+"\nموبایل: "+c.phone+"\nپلاک: "+c.plate+"\nشماره بیمه‌نامه: "+c.policyNo+"\nمبلغ کل: "+money.format(c.total)+" تومان\nپیش‌پرداخت: "+money.format(c.down)+" تومان\nدریافتی: "+money.format(c.paid)+" تومان\nمانده: "+money.format(Math.max(0,c.total-c.paid))+" تومان\nسررسید اولین قسط: "+c.firstDue+"\nاقساط مانده: "+db.remainingInstallmentsForPolicy(id);
        c.close();
        new AlertDialog.Builder(this).setTitle("جزئیات پرونده").setMessage(s).setNegativeButton("بستن",null).setPositiveButton("مشاهده اقساط",(d,w)->{ render("اقساط"); }).show();
    }

    private void installments(){
        body.addView(text("جدول کامل اقساط",21,TEXT));
        body.addView(text("قسط پرداخت‌نشده را انتخاب کنید تا دریافت ثبت شود.",14,MUTED));
        CursorWrap c=new CursorWrap(db.query("SELECT i.id,i.policy_id,i.no,i.amount,i.paid_amount,i.due,p.customer,p.policyNo,p.plate FROM installments i JOIN policies p ON p.id=i.policy_id ORDER BY i.paid ASC, i.due ASC"));
        if(!c.next()){body.addView(text("هنوز قسطی ایجاد نشده است.",18,MUTED));c.close();return;}
        do{
            LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);b.setPadding(dp(14),dp(10),dp(14),dp(10));b.setBackground(bg(WHITE,17));
            b.addView(text(c.customer+" • بیمه‌نامه "+c.policyNo+" • قسط "+c.no,17,TEXT));
            b.addView(text("سررسید: "+c.due+"\nمبلغ قسط: "+money.format(c.amount)+" تومان\nپرداخت‌شده: "+money.format(c.paidAmount)+" تومان",14,MUTED));
            final long iid=c.id; final long amt=c.amount; final int paid=c.paid;
            TextView status;
            if(c.paid==1){ status=chip("✓ پرداخت شده",GREEN); }
            else if(isPast(c.due)){ status=chip("● معوق",RED); }
            else { status=chip("○ باز",ORANGE); }
            b.addView(status);
            if(c.paid==0){ Button p=new Button(this);p.setText("ثبت پرداخت این قسط");p.setAllCaps(false);p.setTextColor(GREEN);p.setBackgroundColor(Color.TRANSPARENT);p.setOnClickListener(new View.OnClickListener(){public void onClick(View v){ installmentPaymentDialog(iid,amt); }});b.addView(p); }
            LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1, c.paid==0?dp(158):dp(130));bp.setMargins(0,0,0,dp(9));body.addView(b,bp);
        }while(c.next()); c.close();
    }

    private void reports(){
        body.addView(text("گزارش مالی و مدیریتی",22,TEXT));
        long total=db.sum("total"), paid=db.sum("paid"), debt=Math.max(0,total-paid);
        LinearLayout r1=row();r1.addView(statCard("فروش کل",money.format(total)+" تومان"));r1.addView(statCard("وصولی",money.format(paid)+" تومان"));body.addView(r1);
        LinearLayout r2=row();r2.addView(statCard("مطالبات",money.format(debt)+" تومان"));r2.addView(statCard("اقساط مانده",String.valueOf(db.remainingInstallments())));body.addView(r2);
        body.addView(text("قسط‌های پرداخت‌شده: "+db.paidInstallments(),16,TEXT));
        body.addView(text("قسط‌های معوق: "+db.overdueInstallments(),16,RED));
        body.addView(text("درصد وصول: "+(total==0?0:(paid*100/total))+"٪",18,GREEN));
        Button share=actionButton("اشتراک گزارش مالی");share.setOnClickListener(new View.OnClickListener(){public void onClick(View v){shareReport();}});body.addView(share);
        Button backup=actionButton("پشتیبان‌گیری اطلاعات");backup.setOnClickListener(new View.OnClickListener(){public void onClick(View v){shareBackup();}});body.addView(backup);
    }

    private void customers(){
        body.addView(text("مشتریان",22,TEXT));
        CursorWrap c=new CursorWrap(db.query("SELECT customer,phone,COUNT(*) cnt,COALESCE(SUM(total-paid),0) debt FROM policies GROUP BY customer,phone ORDER BY customer"));
        if(!c.next()){body.addView(text("مشتری ثبت‌شده‌ای وجود ندارد.",18,MUTED));c.close();return;}
        do{
            LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);b.setPadding(dp(14),dp(11),dp(14),dp(11));b.setBackground(bg(WHITE,16));
            b.addView(text(c.customer,18,TEXT));b.addView(text(c.phone+"  •  تعداد پرونده: "+c.cnt+"\nبدهی: "+money.format(c.debt)+" تومان",14,MUTED));
            LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(105));p.setMargins(0,0,0,dp(9));body.addView(b,p);
        }while(c.next());c.close();
    }

    private void policyDialog(final Long editId){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(6),0,dp(6),0);box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        final EditText customer=input("نام و نام خانوادگی مشتری",InputType.TYPE_CLASS_TEXT);
        final EditText phone=input("شماره موبایل",InputType.TYPE_CLASS_PHONE);
        final EditText plate=input("پلاک خودرو",InputType.TYPE_CLASS_TEXT);
        final EditText policy=input("شماره بیمه‌نامه",InputType.TYPE_CLASS_TEXT);
        final EditText total=input("مبلغ کل بیمه‌نامه (تومان)",InputType.TYPE_CLASS_NUMBER);
        final EditText down=input("پیش‌پرداخت (تومان)",InputType.TYPE_CLASS_NUMBER);
        final EditText count=input("تعداد اقساط",InputType.TYPE_CLASS_NUMBER);
        final EditText first=input("سررسید اولین قسط (YYYY/MM/DD)",InputType.TYPE_CLASS_DATETIME);
        box.addView(customer);box.addView(phone);box.addView(plate);box.addView(policy);box.addView(total);box.addView(down);box.addView(count);box.addView(first);
        if(editId!=null){
            CursorWrap c=new CursorWrap(db.query("SELECT customer,phone,plate,policyNo,total,down,firstDue FROM policies WHERE id="+editId));
            if(c.next()){customer.setText(c.customer);phone.setText(c.phone);plate.setText(c.plate);policy.setText(c.policyNo);total.setText(String.valueOf(c.total));down.setText(String.valueOf(c.down));first.setText(c.firstDue);count.setText(String.valueOf(db.installmentCount(editId)));}c.close();
        } else { first.setText(today()); }
        AlertDialog d=new AlertDialog.Builder(this).setTitle(editId==null?"ثبت بیمه‌نامه جدید":"ویرایش بیمه‌نامه").setView(box).setNegativeButton("انصراف",null).setPositiveButton(editId==null?"ثبت پرونده":"ذخیره تغییرات",null).create();
        d.setOnShowListener(x->{d.getButton(-1).setOnClickListener(v->{
            try{String c=customer.getText().toString().trim(),ph=phone.getText().toString().trim(),pl=plate.getText().toString().trim(),pn=policy.getText().toString().trim(),fd=first.getText().toString().trim(); long tt=Long.parseLong(total.getText().toString().trim()),dd=Long.parseLong(down.getText().toString().trim());int n=Integer.parseInt(count.getText().toString().trim());
                if(c.length()==0||pn.length()==0||tt<=0||dd<0||dd>tt||n<=0||fd.length()<8)throw new Exception();
                if(editId==null) db.addPolicy(c,ph,pl,pn,tt,dd,n,fd); else db.updatePolicy(editId,c,ph,pl,pn,tt,dd,fd,n);
                d.dismiss();render("خانه");
            }catch(Exception e){toast("اطلاعات واردشده کامل یا صحیح نیست");}
        });});d.show();
    }

    private void paymentDialog(){
        CursorWrap c=new CursorWrap(db.query("SELECT id,customer,policyNo,plate FROM policies WHERE total>paid ORDER BY id DESC"));List<Long> ids=new ArrayList<>();List<String> names=new ArrayList<>();
        while(c.next()){ids.add(c.id);names.add(c.customer+" • "+c.policyNo+" • "+c.plate);}c.close();if(names.size()==0){toast("پرونده‌ای با بدهی وجود ندارد");return;}
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);Spinner sp=new Spinner(this);sp.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,names));box.addView(sp);final EditText amount=input("مبلغ دریافت (تومان)",InputType.TYPE_CLASS_NUMBER);box.addView(amount);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("ثبت دریافت").setView(box).setNegativeButton("انصراف",null).setPositiveButton("ثبت دریافت",null).create();d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{try{long a=Long.parseLong(amount.getText().toString());if(a<=0)throw new Exception();db.addPayment(ids.get(sp.getSelectedItemPosition()),a);d.dismiss();render("خانه");}catch(Exception e){toast("مبلغ معتبر نیست");}}));d.show();
    }

    private void installmentPaymentDialog(final long iid, final long amount){
        final EditText e=input("مبلغ پرداختی این قسط",InputType.TYPE_CLASS_NUMBER);e.setText(String.valueOf(amount));
        AlertDialog d=new AlertDialog.Builder(this).setTitle("ثبت پرداخت قسط").setMessage("مبلغ قسط: "+money.format(amount)+" تومان").setView(e).setNegativeButton("انصراف",null).setPositiveButton("ثبت",null).create();d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{try{long a=Long.parseLong(e.getText().toString());if(a<=0)throw new Exception();db.payInstallment(iid,a);d.dismiss();render("اقساط");}catch(Exception ex){toast("مبلغ معتبر نیست");}}));d.show();
    }

    private void searchDialog(){
        final EditText q=input("نام، موبایل، پلاک یا شماره بیمه‌نامه",InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(this).setTitle("جستجوی سریع").setView(q).setNegativeButton("لغو",null).setPositiveButton("جستجو",(d,w)->showSearch(q.getText().toString().trim())).show();
    }
    private void showSearch(String q){
        if(q.length()==0)return; body.removeAllViews();body.addView(text("نتیجه جستجو",22,TEXT));
        CursorWrap c=new CursorWrap(db.query("SELECT id,customer,phone,plate,policyNo,total,paid FROM policies WHERE customer LIKE '%"+esc(q)+"%' OR phone LIKE '%"+esc(q)+"%' OR plate LIKE '%"+esc(q)+"%' OR policyNo LIKE '%"+esc(q)+"%' ORDER BY id DESC"));
        if(!c.next()){body.addView(text("نتیجه‌ای پیدا نشد.",18,MUTED));c.close();return;}do{addPolicyCard(c.id,c.customer,c.phone,c.plate,c.policyNo,c.total,c.paid,false);}while(c.next());c.close();
    }
    private String esc(String s){return s.replace("'","''");}

    private void confirmDelete(final long id){new AlertDialog.Builder(this).setTitle("حذف پرونده").setMessage("همه اقساط پرونده هم حذف می‌شوند. این عملیات قابل بازگشت نیست.").setNegativeButton("انصراف",null).setPositiveButton("حذف",(d,w)->{db.deletePolicy(id);render("پرونده‌ها");}).show();}

    private void settings(){
        new AlertDialog.Builder(this).setTitle("تنظیمات و پشتیبان").setItems(new String[]{"پشتیبان‌گیری","اشتراک گزارش","درباره برنامه"},(d,w)->{if(w==0)shareBackup();else if(w==1)shareReport();else toast("مدیریت حرفه‌ای اقساط بیمه • نسخه ۲۰۲۶.۳");}).show();
    }
    private void shareReport(){String s="گزارش مدیریت اقساط بیمه\nفروش کل: "+money.format(db.sum("total"))+" تومان\nدریافتی: "+money.format(db.sum("paid"))+" تومان\nمطالبات: "+money.format(Math.max(0,db.sum("total")-db.sum("paid")))+" تومان\nپرونده‌ها: "+db.count("policies")+"\nاقساط مانده: "+db.remainingInstallments()+"\nاقساط معوق: "+db.overdueInstallments();share(s,"اشتراک گزارش");}
    private void shareBackup(){share(db.exportText(),"پشتیبان اطلاعات");}
    private void share(String s,String title){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,s);startActivity(Intent.createChooser(i,title));}

    private boolean isPast(String value){try{String[] p=value.split("/");if(p.length!=3)return false;Calendar c=Calendar.getInstance();c.set(Integer.parseInt(p[0]),Integer.parseInt(p[1])-1,Integer.parseInt(p[2]),23,59,59);return c.getTime().before(new Date());}catch(Exception e){return false;}}
    private String today(){return new SimpleDateFormat("yyyy/MM/dd",Locale.US).format(new Date());}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}

    static class CursorWrap{
        android.database.Cursor cur;long id,policyId,total,down,paid,debt,amount,paidAmount;int no,cnt;String customer,phone,plate,policyNo,firstDue,due; 
        CursorWrap(android.database.Cursor c){cur=c;}
        boolean next(){if(!cur.moveToNext())return false;android.database.Cursor c=cur;for(int i=0;i<c.getColumnCount();i++){String n=c.getColumnName(i);if(n.equals("id"))id=c.getLong(i);else if(n.equals("policy_id"))policyId=c.getLong(i);else if(n.equals("customer"))customer=c.getString(i);else if(n.equals("phone"))phone=c.getString(i);else if(n.equals("plate"))plate=c.getString(i);else if(n.equals("policyNo"))policyNo=c.getString(i);else if(n.equals("total"))total=c.getLong(i);else if(n.equals("down"))down=c.getLong(i);else if(n.equals("paid"))paid=c.getInt(i);else if(n.equals("paid_amount"))paidAmount=c.getLong(i);else if(n.equals("amount"))amount=c.getLong(i);else if(n.equals("no"))no=c.getInt(i);else if(n.equals("due"))due=c.getString(i);else if(n.equals("firstDue"))firstDue=c.getString(i);else if(n.equals("cnt"))cnt=c.getInt(i);else if(n.equals("debt"))debt=c.getLong(i);}return true;}
        void close(){cur.close();}
    }

    static class DB extends android.database.sqlite.SQLiteOpenHelper{
        DB(Context c){super(c,"bimeh_pro_2026.db",null,3);}
        public void onCreate(android.database.sqlite.SQLiteDatabase d){
            d.execSQL("CREATE TABLE policies(id INTEGER PRIMARY KEY AUTOINCREMENT,customer TEXT,phone TEXT,plate TEXT,policyNo TEXT UNIQUE,total INTEGER,down INTEGER,paid INTEGER DEFAULT 0,firstDue TEXT)");
            d.execSQL("CREATE TABLE installments(id INTEGER PRIMARY KEY AUTOINCREMENT,policy_id INTEGER,no INTEGER,amount INTEGER,paid_amount INTEGER DEFAULT 0,due TEXT,paid INTEGER DEFAULT 0)");
            d.execSQL("CREATE TABLE payments(id INTEGER PRIMARY KEY AUTOINCREMENT,policy_id INTEGER,installment_id INTEGER,amount INTEGER,created INTEGER)");
        }
        public void onUpgrade(android.database.sqlite.SQLiteDatabase d,int oldV,int newV){if(oldV<3){try{d.execSQL("ALTER TABLE installments ADD COLUMN paid_amount INTEGER DEFAULT 0");}catch(Exception ignored){}}}
        android.database.Cursor query(String sql){return getReadableDatabase().rawQuery(sql,null);}
        long sum(String col){android.database.Cursor c=query("SELECT COALESCE(SUM("+col+"),0) FROM policies");long x=c.moveToFirst()?c.getLong(0):0;c.close();return x;}
        int count(String t){android.database.Cursor c=query("SELECT COUNT(*) FROM "+t);int x=c.moveToFirst()?c.getInt(0):0;c.close();return x;}
        int remainingInstallments(){android.database.Cursor c=query("SELECT COUNT(*) FROM installments WHERE paid=0");int x=c.moveToFirst()?c.getInt(0):0;c.close();return x;}
        int paidInstallments(){android.database.Cursor c=query("SELECT COUNT(*) FROM installments WHERE paid=1");int x=c.moveToFirst()?c.getInt(0):0;c.close();return x;}
        int overdueInstallments(){android.database.Cursor c=query("SELECT COUNT(*) FROM installments WHERE paid=0 AND due < date('now')");int x=c.moveToFirst()?c.getInt(0):0;c.close();return x;}
        int remainingInstallmentsForPolicy(long id){android.database.Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM installments WHERE policy_id=? AND paid=0",new String[]{String.valueOf(id)});int x=c.moveToFirst()?c.getInt(0):0;c.close();return x;}
        int installmentCount(long id){android.database.Cursor c=getReadableDatabase().rawQuery("SELECT COUNT(*) FROM installments WHERE policy_id=?",new String[]{String.valueOf(id)});int x=c.moveToFirst()?c.getInt(0):0;c.close();return x;}
        long addPolicy(String customer,String phone,String plate,String policyNo,long total,long down,int n,String first){android.database.sqlite.SQLiteDatabase d=getWritableDatabase();android.content.ContentValues v=new android.content.ContentValues();v.put("customer",customer);v.put("phone",phone);v.put("plate",plate);v.put("policyNo",policyNo);v.put("total",total);v.put("down",down);v.put("paid",down);v.put("firstDue",first);long id=d.insertOrThrow("policies",null,v);createInstallments(id,total-down,n,first);return id;}
        void createInstallments(long policy,long remaining,int n,String first){android.database.sqlite.SQLiteDatabase d=getWritableDatabase();long each=remaining/n;long rem=remaining;for(int i=1;i<=n;i++){long a=(i==n)?rem:each;android.content.ContentValues x=new android.content.ContentValues();x.put("policy_id",policy);x.put("no",i);x.put("amount",a);x.put("paid_amount",0);x.put("due",addMonth(first,i-1));x.put("paid",0);d.insert("installments",null,x);rem-=a;}}
        String addMonth(String base,int m){try{String[] p=base.split("/");Calendar c=Calendar.getInstance();c.set(Integer.parseInt(p[0]),Integer.parseInt(p[1])-1,Integer.parseInt(p[2]));c.add(Calendar.MONTH,m);return String.format(Locale.US,"%04d/%02d/%02d",c.get(Calendar.YEAR),c.get(Calendar.MONTH)+1,c.get(Calendar.DAY_OF_MONTH));}catch(Exception e){return base;}}
        void updatePolicy(long id,String c,String ph,String pl,String pn,long total,long down,String first,int n){android.database.sqlite.SQLiteDatabase d=getWritableDatabase();android.content.ContentValues v=new android.content.ContentValues();v.put("customer",c);v.put("phone",ph);v.put("plate",pl);v.put("policyNo",pn);v.put("total",total);v.put("down",down);v.put("firstDue",first);long paid=down;android.database.Cursor q=d.rawQuery("SELECT COALESCE(SUM(amount),0) FROM installments WHERE policy_id=? AND paid=1",new String[]{String.valueOf(id)});if(q.moveToFirst())paid+=q.getLong(0);q.close();v.put("paid",Math.min(total,paid));d.update("policies",v,"id=?",new String[]{String.valueOf(id)});d.delete("installments","policy_id=? AND paid=0",new String[]{String.valueOf(id)});int existing=installmentCount(id);int missing=Math.max(0,n-existing);if(missing>0)createInstallments(id,Math.max(0,total-paid),missing,first);}
        void addPayment(long policy,long amount){android.database.sqlite.SQLiteDatabase d=getWritableDatabase();android.database.Cursor c=d.rawQuery("SELECT paid,total FROM policies WHERE id=?",new String[]{String.valueOf(policy)});if(!c.moveToFirst()){c.close();return;}long p=c.getLong(0),t=c.getLong(1);c.close();long a=Math.min(amount,Math.max(0,t-p));android.content.ContentValues v=new android.content.ContentValues();v.put("paid",p+a);d.update("policies",v,"id=?",new String[]{String.valueOf(policy)});d.insert("payments",null,values(policy,0,a));applyToOldest(policy,a);}
        private android.content.ContentValues values(long p,long i,long a){android.content.ContentValues v=new android.content.ContentValues();v.put("policy_id",p);if(i>0)v.put("installment_id",i);v.put("amount",a);v.put("created",System.currentTimeMillis());return v;}
        private void applyToOldest(long policy,long amount){android.database.sqlite.SQLiteDatabase d=getWritableDatabase();android.database.Cursor c=d.rawQuery("SELECT id,amount,paid_amount FROM installments WHERE policy_id=? AND paid=0 ORDER BY no",new String[]{String.valueOf(policy)});long left=amount;while(left>0&&c.moveToNext()){long id=c.getLong(0),a=c.getLong(1),pp=c.getLong(2),need=a-pp,put=Math.min(need,left);android.content.ContentValues v=new android.content.ContentValues();v.put("paid_amount",pp+put);v.put("paid",pp+put>=a?1:0);d.update("installments",v,"id=?",new String[]{String.valueOf(id)});left-=put;}c.close();}
        void payInstallment(long iid,long amount){android.database.sqlite.SQLiteDatabase d=getWritableDatabase();android.database.Cursor c=d.rawQuery("SELECT policy_id,amount,paid_amount,paid FROM installments WHERE id=?",new String[]{String.valueOf(iid)});if(!c.moveToFirst()){c.close();return;}long policy=c.getLong(0),a=c.getLong(1),pp=c.getLong(2);c.close();long put=Math.min(amount,Math.max(0,a-pp));android.content.ContentValues v=new android.content.ContentValues();v.put("paid_amount",pp+put);v.put("paid",pp+put>=a?1:0);d.update("installments",v,"id=?",new String[]{String.valueOf(iid)});            android.database.Cursor q=d.rawQuery("SELECT paid,total FROM policies WHERE id=?",new String[]{String.valueOf(policy)});if(q.moveToFirst()){long cur=q.getLong(0),tot=q.getLong(1);android.content.ContentValues pv=new android.content.ContentValues();pv.put("paid",Math.min(tot,cur+put));d.update("policies",pv,"id=?",new String[]{String.valueOf(policy)});}q.close();d.insert("payments",null,values(policy,iid,put));}
        void deletePolicy(long id){android.database.sqlite.SQLiteDatabase d=getWritableDatabase();d.delete("payments","policy_id=?",new String[]{String.valueOf(id)});d.delete("installments","policy_id=?",new String[]{String.valueOf(id)});d.delete("policies","id=?",new String[]{String.valueOf(id)});}
        String exportText(){StringBuilder s=new StringBuilder("پشتیبان مدیریت اقساط بیمه 2026\n");android.database.Cursor c=query("SELECT customer,phone,plate,policyNo,total,paid FROM policies ORDER BY id");while(c.moveToNext())s.append(c.getString(0)).append(" | ").append(c.getString(1)).append(" | ").append(c.getString(2)).append(" | ").append(c.getString(3)).append(" | کل ").append(c.getLong(4)).append(" | پرداخت ").append(c.getLong(5)).append("\n");c.close();return s.toString();}
    }
}
