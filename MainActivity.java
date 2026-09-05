package ir.bimeh.installments;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private DB db;
    private LinearLayout screen;
    private TextView title;
    private NumberFormat money = NumberFormat.getInstance(new Locale("fa","IR"));
    private int green = Color.rgb(11,107,83), dark = Color.rgb(6,76,60), bg = Color.rgb(245,247,246);
    private int text = Color.rgb(23,33,30), muted = Color.rgb(100,113,108);

    @Override public void onCreate(Bundle b){ super.onCreate(b); db=new DB(this); render("خانه"); }

    int dp(int x){ return (int)(x*getResources().getDisplayMetrics().density+.5f); }
    TextView tv(String s,float size){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(text); t.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); t.setPadding(dp(12),dp(8),dp(12),dp(8)); return t; }
    MaterialButton btn(String s){ MaterialButton b=new MaterialButton(this); b.setText(s); b.setTextSize(15); b.setAllCaps(false); b.setTextColor(Color.WHITE); b.setBackgroundColor(green); b.setCornerRadius(dp(16)); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(54)); p.setMargins(0,dp(6),0,dp(6)); b.setLayoutParams(p); return b; }
    MaterialCardView card(){ MaterialCardView c=new MaterialCardView(this); c.setRadius(dp(18)); c.setCardElevation(dp(2)); c.setUseCompatPadding(true); c.setCardBackgroundColor(Color.WHITE); return c; }

    void render(String page){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(bg); root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout top=new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL); top.setPadding(dp(16),dp(8),dp(16),dp(8)); top.setBackgroundColor(dark);
        title=tv(page,20); title.setTextColor(Color.WHITE); title.setTypeface(Typeface.DEFAULT,Typeface.BOLD); top.addView(title,new LinearLayout.LayoutParams(0,dp(64),1));
        TextView gear=tv("⚙",25); gear.setTextColor(Color.WHITE); gear.setGravity(Gravity.CENTER); gear.setOnClickListener(v->settingsDialog()); top.addView(gear,new LinearLayout.LayoutParams(dp(48),dp(64))); root.addView(top);
        FrameLayout center=new FrameLayout(this); screen=new LinearLayout(this); screen.setOrientation(LinearLayout.VERTICAL); screen.setPadding(dp(14),dp(14),dp(14),dp(14)); screen.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); ScrollView sv=new ScrollView(this); sv.addView(screen); center.addView(sv); root.addView(center,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout nav=new LinearLayout(this); nav.setOrientation(LinearLayout.HORIZONTAL); nav.setPadding(dp(6),dp(5),dp(6),dp(7)); nav.setBackgroundColor(Color.WHITE);
        String[] ns={"خانه","بیمه‌نامه","اقساط","گزارش","مشتریان"};
        for(String n:ns){ MaterialButton b=new MaterialButton(this); b.setText(n); b.setAllCaps(false); b.setTextSize(13); b.setTextColor(n.equals(page)?green:muted); b.setBackgroundColor(Color.TRANSPARENT); b.setOnClickListener(v->render(n)); nav.addView(b,new LinearLayout.LayoutParams(0,dp(58),1)); }
        root.addView(nav); setContentView(root);
        if(page.equals("خانه")) home(); else if(page.equals("بیمه‌نامه")) policies(); else if(page.equals("اقساط")) installments(); else if(page.equals("گزارش")) reports(); else customers();
    }

    void home(){
        screen.addView(tv("مدیریت حرفه‌ای اقساط بیمه ثالث",24));
        screen.addView(tv("نسخه ۲۰۲۶ • دفتر نمایندگی",14));
        LinearLayout stats=new LinearLayout(this); stats.setOrientation(LinearLayout.HORIZONTAL); long total=db.sum("total"), paid=db.sum("paid"), debt=total-paid; int count=db.count("policies");
        stats.addView(stat("پرونده",String.valueOf(count))); stats.addView(stat("دریافتی",money.format(paid))); stats.addView(stat("مطالبات",money.format(debt))); screen.addView(stats);
        MaterialButton add=btn("＋ ثبت بیمه‌نامه جدید"); add.setOnClickListener(v->policyDialog()); screen.addView(add);
        MaterialButton pay=btn("✓ ثبت دریافت قسط"); pay.setOnClickListener(v->paymentDialog()); screen.addView(pay);
        MaterialButton due=btn("⏰ اقساط نزدیک سررسید"); due.setOnClickListener(v->render("اقساط")); screen.addView(due);
        TextView quick=tv("عملیات سریع",18); quick.setTypeface(null,Typeface.BOLD); screen.addView(quick);
        String[][] q={{"👤 مشتری جدید","افزودن پرونده مشتری"},{"▤ گزارش مالی","مشاهده جمع فروش و وصولی"},{"🔎 جستجو","جستجوی نام، موبایل یا پلاک"}};
        for(String[] x:q){ MaterialCardView c=card(); TextView t=tv(x[0]+"\n"+x[1],16); c.addView(t); c.setOnClickListener(v->{ if(x[0].contains("مشتری")) policyDialog(); else if(x[0].contains("گزارش")) render("گزارش"); else searchDialog();}); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(82)); p.setMargins(0,dp(5),0,dp(5)); screen.addView(c,p); }
    }
    View stat(String a,String v){ MaterialCardView c=card(); TextView t=tv(a+"\n"+v,15); t.setTypeface(null,Typeface.BOLD); c.addView(t); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(90),1); p.setMargins(dp(3),0,dp(3),dp(10)); c.setLayoutParams(p); return c; }

    void policies(){ screen.addView(tv("پرونده بیمه‌نامه‌ها",22)); MaterialButton add=btn("＋ بیمه‌نامه جدید"); add.setOnClickListener(v->policyDialog()); screen.addView(add); Cursor c=db.query("SELECT id,customer,phone,plate,policyNo,total,down,paid FROM policies ORDER BY id DESC"); while(c.moveToNext()){ long id=c.getLong(0); MaterialCardView card=card(); LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.addView(tv("مشتری: "+c.getString(1),17)); box.addView(tv("بیمه‌نامه: "+c.getString(5)+"  •  پلاک: "+c.getString(3),14)); box.addView(tv("کل: "+money.format(c.getLong(6))+"  •  دریافتی: "+money.format(c.getLong(7))+"  •  مانده: "+money.format(c.getLong(6)-c.getLong(7)),14)); MaterialButton del=btn("حذف پرونده"); del.setOnClickListener(v->confirmDelete(id)); box.addView(del); card.addView(box); screen.addView(card,params(0,dp(176),0,dp(9))); } c.close(); }

    void installments(){ screen.addView(tv("تقویم اقساط و سررسیدها",22)); Cursor c=db.query("SELECT i.id,p.customer,p.plate,i.no,i.amount,i.due,i.paid FROM installments i JOIN policies p ON p.id=i.policy_id ORDER BY i.paid ASC,i.due ASC"); boolean any=false; while(c.moveToNext()){ any=true; MaterialCardView card=card(); String status=c.getInt(6)==1?"✓ پرداخت شده":isPast(c.getString(5))?"⚠ معوق":"● در انتظار"; TextView t=tv(c.getString(1)+" • "+c.getString(2)+"\nقسط "+c.getInt(3)+"  |  "+money.format(c.getLong(4))+" تومان\nسررسید: "+c.getString(5)+"  |  "+status,15); if(c.getInt(6)==0) t.setTextColor(isPast(c.getString(5))?Color.rgb(170,60,35):text); card.addView(t); card.setOnClickListener(v->paymentForInstallment(c.getLong(0),c.getLong(4))); screen.addView(card,params(0,dp(108),0,dp(8))); } c.close(); if(!any) screen.addView(tv("هنوز قسطی ایجاد نشده است.",17)); }

    void reports(){ screen.addView(tv("داشبورد مالی",22)); long total=db.sum("total"), paid=db.sum("paid"), debt=total-paid; screen.addView(stat("فروش کل",money.format(total))); screen.addView(stat("وصولی",money.format(paid))); screen.addView(stat("مطالبات",money.format(debt))); screen.addView(tv("نرخ وصولی: "+(total==0?0:(paid*100/total))+"٪",18)); screen.addView(tv("تعداد بیمه‌نامه: "+db.count("policies"),17)); screen.addView(tv("اقساط پرداخت‌شده: "+db.paidInstallments(),17)); screen.addView(tv("اقساط معوق: "+db.overdueInstallments(),17)); MaterialButton share=btn("↗ اشتراک گزارش"); share.setOnClickListener(v->shareReport()); screen.addView(share); }

    void customers(){ screen.addView(tv("مشتریان",22)); MaterialButton search=btn("🔎 جستجوی مشتری"); search.setOnClickListener(v->searchDialog()); screen.addView(search); Cursor c=db.query("SELECT customer,phone,COUNT(*) FROM policies GROUP BY customer,phone ORDER BY customer"); while(c.moveToNext()){ MaterialCardView card=card(); card.addView(tv(c.getString(0)+"\n"+c.getString(1)+"  •  "+c.getInt(2)+" بیمه‌نامه",16)); screen.addView(card,params(0,dp(88),0,dp(7))); } c.close(); }

    LinearLayout.LayoutParams params(int l,int h,int r,int b){ LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,h); p.setMargins(dp(l),dp(5),dp(r),dp(b)); return p; }
    TextInputLayout til(String hint){ TextInputLayout t=new TextInputLayout(this); t.setHint(hint); t.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE); TextInputEditText e=new TextInputEditText(t.getContext()); e.setTextSize(16); e.setGravity(Gravity.RIGHT); t.addView(e,new LinearLayout.LayoutParams(-1,dp(58))); return t; }
    String val(TextInputLayout t){ return String.valueOf(((TextInputEditText)t.getEditText()).getText()).trim(); }
    long num(TextInputLayout t){ try{return Long.parseLong(val(t).replace(",",""));}catch(Exception e){return 0;} }

    void policyDialog(){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(4),0,dp(4),0); box.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        TextInputLayout c=til("نام و نام خانوادگی"); TextInputLayout ph=til("موبایل"); ph.getEditText().setInputType(InputType.TYPE_CLASS_PHONE); TextInputLayout pl=til("پلاک خودرو"); TextInputLayout pn=til("شماره بیمه‌نامه"); TextInputLayout total=til("مبلغ کل بیمه‌نامه (تومان)"); total.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER); TextInputLayout down=til("پیش‌پرداخت (تومان)"); down.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER); TextInputLayout count=til("تعداد اقساط"); count.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER); TextInputLayout first=til("تاریخ اولین سررسید (مثلاً 1405/07/01)");
        box.addView(c);box.addView(ph);box.addView(pl);box.addView(pn);box.addView(total);box.addView(down);box.addView(count);box.addView(first);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("ثبت بیمه‌نامه جدید").setView(box).setNegativeButton("انصراف",null).setPositiveButton("ثبت پرونده",null).create(); d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{ long tt=num(total),dd=num(down); int n=(int)num(count); if(val(c).isEmpty()||tt<=0||n<=0||dd<0||dd>tt){ total.setError("اطلاعات واردشده صحیح نیست"); return;} long id=db.addPolicy(val(c),val(ph),val(pl),val(pn),tt,dd,n,val(first)); d.dismiss(); render("خانه"); }); d.show(); }

    void paymentDialog(){ Cursor c=db.query("SELECT id,customer,plate FROM policies WHERE total>paid ORDER BY id DESC"); ArrayList<Long> ids=new ArrayList<>(); ArrayList<String> names=new ArrayList<>(); while(c.moveToNext()){ids.add(c.getLong(0));names.add(c.getString(1)+" • "+c.getString(2));} c.close(); if(names.size()==0){toast("پرونده‌ای با مانده بدهی وجود ندارد");return;} LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL); Spinner sp=new Spinner(this);sp.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names)); box.addView(sp); TextInputLayout a=til("مبلغ دریافت (تومان)");a.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);box.addView(a); AlertDialog d=new AlertDialog.Builder(this).setTitle("ثبت دریافت").setView(box).setNegativeButton("انصراف",null).setPositiveButton("ثبت دریافت",null).create(); d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{long amount=num(a);if(amount<=0){a.setError("مبلغ نامعتبر");return;}db.addPayment(ids.get(sp.getSelectedItemPosition()),amount);d.dismiss();render("خانه");}));d.show(); }
    void paymentForInstallment(long id,long amount){ AlertDialog d=new AlertDialog.Builder(this).setTitle("تسویه قسط").setMessage("مبلغ این قسط: "+money.format(amount)+" تومان\nآیا پرداخت شد؟").setNegativeButton("لغو",null).setPositiveButton("ثبت پرداخت",(x,w)->{db.payInstallment(id);render("اقساط");}).create();d.show(); }

    void confirmDelete(long id){new AlertDialog.Builder(this).setTitle("حذف پرونده").setMessage("این عملیات قابل بازگشت نیست.").setNegativeButton("لغو",null).setPositiveButton("حذف",(d,w)->{db.deletePolicy(id);render("بیمه‌نامه");}).show();}
    void searchDialog(){ TextInputLayout q=til("نام، موبایل، پلاک یا شماره بیمه‌نامه"); new AlertDialog.Builder(this).setTitle("جستجو").setView(q).setNegativeButton("لغو",null).setPositiveButton("جستجو",(d,w)->showSearch(val(q))).show(); }
    void showSearch(String q){ if(q.isEmpty())return; screen.removeAllViews();screen.addView(tv("نتیجه جستجو",22)); Cursor c=db.query("SELECT customer,phone,plate,policyNo,total,paid FROM policies WHERE customer LIKE ? OR phone LIKE ? OR plate LIKE ? OR policyNo LIKE ? ORDER BY id DESC",new String[]{"%"+q+"%","%"+q+"%","%"+q+"%","%"+q+"%"}); while(c.moveToNext()){MaterialCardView card=card();card.addView(tv(c.getString(0)+"\n"+c.getString(1)+" • "+c.getString(2)+"\nمانده: "+money.format(c.getLong(4)-c.getLong(5))+" تومان",16));screen.addView(card,params(0,dp(105),0,dp(8)));}c.close();}
    void settingsDialog(){ new AlertDialog.Builder(this).setTitle("تنظیمات").setItems(new String[]{"پشتیبان‌گیری اطلاعات","اشتراک گزارش مالی","اطلاعات برنامه","شناسه برنامه: "+Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID)},(d,w)->{if(w==0)shareBackup();else if(w==1)shareReport();else if(w==2)toast("مدیریت اقساط بیمه • نسخه 2026.1");}).show(); }
    void shareReport(){String s="گزارش مالی مدیریت اقساط بیمه\nفروش کل: "+money.format(db.sum("total"))+" تومان\nدریافتی: "+money.format(db.sum("paid"))+" تومان\nمطالبات: "+money.format(db.sum("total")-db.sum("paid"))+" تومان\nتعداد پرونده: "+db.count("policies"); Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,s);startActivity(Intent.createChooser(i,"اشتراک گزارش"));}
    void shareBackup(){String s=db.exportText();Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,s);startActivity(Intent.createChooser(i,"پشتیبان اطلاعات"));}
    boolean isPast(String d){ try{String[] p=d.split("/");if(p.length!=3)return false;SimpleDateFormat f=new SimpleDateFormat("yyyy/MM/dd",Locale.US);Date x=f.parse((Integer.parseInt(p[0])+621)+"/"+p[1]+"/"+p[2]);return x.before(new Date());}catch(Exception e){return false;} }
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}

    static class DB extends SQLiteOpenHelper {
        DB(Context c){super(c,"bimeh2026.db",null,2);}
        public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE policies(id INTEGER PRIMARY KEY AUTOINCREMENT,customer TEXT,phone TEXT,plate TEXT,policyNo TEXT,total INTEGER,down INTEGER,paid INTEGER DEFAULT 0,firstDue TEXT)");d.execSQL("CREATE TABLE installments(id INTEGER PRIMARY KEY AUTOINCREMENT,policy_id INTEGER,no INTEGER,amount INTEGER,due TEXT,paid INTEGER DEFAULT 0)");d.execSQL("CREATE TABLE payments(id INTEGER PRIMARY KEY AUTOINCREMENT,policy_id INTEGER,amount INTEGER,created INTEGER)");}
        public void onUpgrade(SQLiteDatabase d,int a,int b){}
        Cursor query(String s){return getReadableDatabase().rawQuery(s,null);} Cursor query(String s,String[] a){return getReadableDatabase().rawQuery(s,a);}
        long addPolicy(String c,String ph,String pl,String pn,long total,long down,int n,String first){SQLiteDatabase d=getWritableDatabase();long paid=down;android.content.ContentValues v=new android.content.ContentValues();v.put("customer",c);v.put("phone",ph);v.put("plate",pl);v.put("policyNo",pn);v.put("total",total);v.put("down",down);v.put("paid",paid);v.put("firstDue",first);long id=d.insert("policies",null,v);long rem=total-down;long each=n==0?0:((rem+n-1)/n);for(int i=1;i<=n;i++){android.content.ContentValues x=new android.content.ContentValues();x.put("policy_id",id);x.put("no",i);x.put("amount",i==n?rem-each*(n-1):each);x.put("due",addMonth(first,i-1));d.insert("installments",null,x);}return id;}
        String addMonth(String base,int m){try{String[] p=base.split("/");Calendar c=Calendar.getInstance();c.set(Integer.parseInt(p[0])+621,Integer.parseInt(p[1])-1,Integer.parseInt(p[2]));c.add(Calendar.MONTH,m);return (c.get(Calendar.YEAR)-621)+"/"+String.format(Locale.US,"%02d",c.get(Calendar.MONTH)+1)+"/"+String.format(Locale.US,"%02d",c.get(Calendar.DAY_OF_MONTH));}catch(Exception e){return base;}}
        long sum(String col){Cursor c=query("SELECT COALESCE(SUM("+col+"),0) FROM policies");long x=0;if(c.moveToFirst())x=c.getLong(0);c.close();return x;}
        int count(String table){Cursor c=query("SELECT COUNT(*) FROM "+table);int x=0;if(c.moveToFirst())x=c.getInt(0);c.close();return x;}
        int paidInstallments(){Cursor c=query("SELECT COUNT(*) FROM installments WHERE paid=1");int x=0;if(c.moveToFirst())x=c.getInt(0);c.close();return x;}
        int overdueInstallments(){Cursor c=query("SELECT COUNT(*) FROM installments WHERE paid=0");int x=0;while(c.moveToNext()){} c.close();return x;}
        void addPayment(long policy,long amount){SQLiteDatabase d=getWritableDatabase();Cursor c=d.rawQuery("SELECT paid,total FROM policies WHERE id=?",new String[]{String.valueOf(policy)});if(!c.moveToFirst()){c.close();return;}long p=c.getLong(0),t=c.getLong(1);c.close();ContentValues v=new ContentValues();v.put("paid",Math.min(t,p+amount));d.update("policies",v,"id=?",new String[]{String.valueOf(policy)});}
        void payInstallment(long id){SQLiteDatabase d=getWritableDatabase();Cursor c=d.rawQuery("SELECT policy_id,amount,paid FROM installments WHERE id=?",new String[]{String.valueOf(id)});if(!c.moveToFirst()){c.close();return;}long p=c.getLong(0),a=c.getLong(1);int done=c.getInt(2);c.close();if(done==1)return;ContentValues i=new ContentValues();i.put("paid",1);d.update("installments",i,"id=?",new String[]{String.valueOf(id)});Cursor q=d.rawQuery("SELECT paid,total FROM policies WHERE id=?",new String[]{String.valueOf(p)});if(q.moveToFirst()){ContentValues v=new ContentValues();v.put("paid",Math.min(q.getLong(1),q.getLong(0)+a));d.update("policies",v,"id=?",new String[]{String.valueOf(p)});}q.close();}
        void deletePolicy(long id){SQLiteDatabase d=getWritableDatabase();d.delete("installments","policy_id=?",new String[]{String.valueOf(id)});d.delete("payments","policy_id=?",new String[]{String.valueOf(id)});d.delete("policies","id=?",new String[]{String.valueOf(id)});}
        String exportText(){StringBuilder s=new StringBuilder("پشتیبان مدیریت اقساط بیمه 2026\n");Cursor c=query("SELECT customer,phone,plate,policyNo,total,paid FROM policies ORDER BY id");while(c.moveToNext())s.append(c.getString(0)).append(" | ").append(c.getString(1)).append(" | ").append(c.getString(2)).append(" | ").append(c.getString(3)).append(" | کل ").append(c.getLong(4)).append(" | پرداخت ").append(c.getLong(5)).append("\n");c.close();return s.toString();}
    }
}
