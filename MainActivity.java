package ir.bimeh.installments;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private DB db;
    private LinearLayout content;
    private TextView totalPolicies, totalReceived, totalDebt, overdueCount;
    private final int NAVY=Color.rgb(11,31,51), BLUE=Color.rgb(21,101,216), CYAN=Color.rgb(18,184,200);
    private final int GREEN=Color.rgb(23,166,115), ORANGE=Color.rgb(245,158,11), RED=Color.rgb(225,77,90);
    private final int BG=Color.rgb(244,247,251), CARD=Color.WHITE, TEXT=Color.rgb(18,35,58), MUTED=Color.rgb(107,122,144), LINE=Color.rgb(229,234,242);

    @Override public void onCreate(Bundle b){ super.onCreate(b); db=new DB(this); dashboard(); }

    TextView text(String s,float sp,int color){ TextView t=new TextView(this); t.setText(s); t.setTextSize(sp); t.setTextColor(color); t.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); t.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return t; }
    TextView title(String s){ TextView t=text(s,22,Color.WHITE); t.setTypeface(null,1); t.setPadding(18,8,18,8); return t; }
    GradientDrawable bg(int color,float radius){ GradientDrawable g=new GradientDrawable(); g.setColor(color); g.setCornerRadius(radius); return g; }
    View space(int h){ Space s=new Space(this); s.setLayoutParams(new LinearLayout.LayoutParams(1,h)); return s; }
    LinearLayout vbox(){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return l; }
    LinearLayout hbox(){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return l; }
    TextView label(String s){ TextView t=text(s,13,MUTED); t.setPadding(0,0,0,6); return t; }

    void shell(String pageTitle){
        LinearLayout root=vbox(); root.setBackgroundColor(BG);
        LinearLayout header=hbox(); header.setPadding(16,16,16,16); header.setBackground(bg(NAVY,0));
        TextView back=text("‹",38,Color.WHITE); back.setGravity(Gravity.CENTER); back.setOnClickListener(v->dashboard());
        header.addView(back,new LinearLayout.LayoutParams(52,64));
        header.addView(title(pageTitle),new LinearLayout.LayoutParams(0,64,1));
        root.addView(header);
        ScrollView sc=new ScrollView(this); sc.setFillViewport(true); content=vbox(); content.setPadding(18,18,18,28); sc.addView(content); root.addView(sc,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
    }

    void dashboard(){
        LinearLayout root=vbox(); root.setBackgroundColor(BG);
        LinearLayout hero=vbox(); hero.setPadding(22,28,22,24); hero.setBackground(gradient(NAVY,BLUE));
        TextView brand=text("مدیریت اقساط بیمه ثالث",26,Color.WHITE); brand.setTypeface(null,1); hero.addView(brand);
        TextView sub=text("داشبورد حرفه‌ای نمایندگی",14,Color.rgb(205,220,235)); hero.addView(sub);
        LinearLayout actions=hbox(); actions.setPadding(0,18,0,0);
        TextView add=action("＋\nبیمه‌نامه جدید",CYAN); TextView report=action("▣\nگزارش مالی",GREEN);
        actions.addView(add,new LinearLayout.LayoutParams(0,86,1)); actions.addView(spaceW(10),new LinearLayout.LayoutParams(10,1)); actions.addView(report,new LinearLayout.LayoutParams(0,86,1));
        hero.addView(actions); root.addView(hero);
        ScrollView sc=new ScrollView(this); LinearLayout body=vbox(); body.setPadding(18,18,18,28); sc.addView(body); root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));

        TextView sec=text("خلاصه وضعیت",17,TEXT); sec.setTypeface(null,1); body.addView(sec); body.addView(space(10));
        LinearLayout row=hbox();
        totalPolicies=metricCard("بیمه‌نامه‌ها","0",BLUE); totalReceived=metricCard("دریافتی","0 تومان",GREEN); row.addView(totalPolicies,new LinearLayout.LayoutParams(0,112,1)); row.addView(spaceW(10),new LinearLayout.LayoutParams(10,1)); row.addView(totalReceived,new LinearLayout.LayoutParams(0,112,1)); body.addView(row);
        LinearLayout row2=hbox(); totalDebt=metricCard("مانده مطالبات","0 تومان",RED); overdueCount=metricCard("اقساط معوق","0",ORANGE); row2.addView(totalDebt,new LinearLayout.LayoutParams(0,112,1)); row2.addView(spaceW(10),new LinearLayout.LayoutParams(10,1)); row2.addView(overdueCount,new LinearLayout.LayoutParams(0,112,1)); body.addView(row2);
        body.addView(space(16));
        TextView actionsTitle=text("دسترسی سریع",17,TEXT); actionsTitle.setTypeface(null,1); body.addView(actionsTitle); body.addView(space(10));
        TextView list=menuCard("▤", "لیست بیمه‌نامه‌ها", "جستجو، مشاهده جزئیات و ثبت پرداخت", BLUE); body.addView(list); list.setOnClickListener(v->listPolicies());
        TextView next=menuCard("◷", "سررسیدهای نزدیک", "مشاهده اقساط آینده و وضعیت پرداخت", CYAN); body.addView(next); next.setOnClickListener(v->installmentBoard());
        TextView search=menuCard("⌕", "جستجوی مشتری", "جستجو بر اساس نام یا پلاک", GREEN); body.addView(search); search.setOnClickListener(v->searchPolicies());
        refreshDashboard(); setContentView(root);
    }

    GradientDrawable gradient(int c1,int c2){ GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{c1,c2}); g.setCornerRadius(0); return g; }
    Space spaceW(int w){ Space s=new Space(this); s.setMinimumWidth(w); return s; }
    TextView action(String s,int color){ TextView t=text(s,15,Color.WHITE); t.setGravity(Gravity.CENTER); t.setTypeface(null,1); t.setBackground(bg(color,18)); return t; }
    TextView metricCard(String label,String value,int accent){ TextView box=text(label+"\n"+value,14,MUTED); box.setPadding(16,14,16,14); box.setTypeface(null,1); box.setBackground(bg(CARD,18)); box.setTextColor(TEXT); return box; }
    TextView menuCard(String icon,String a,String b,int accent){ TextView t=text(icon+"   "+a+"\n             "+b,16,TEXT); t.setPadding(18,10,18,10); t.setBackground(bg(CARD,18)); return t; }

    void refreshDashboard(){ totalPolicies.setText("بیمه‌نامه‌ها\n"+db.countPolicies()); totalReceived.setText("دریافتی\n"+money(db.sumPaid())+" تومان"); totalDebt.setText("مانده مطالبات\n"+money(db.sumDebt())+" تومان"); overdueCount.setText("اقساط معوق\n"+db.overdue()); }

    EditText input(String hint,int type){ EditText e=new EditText(this); e.setHint(hint); e.setTextSize(15); e.setSingleLine(true); e.setPadding(18,4,18,4); e.setInputType(type); e.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); e.setTextDirection(View.TEXT_DIRECTION_RTL); e.setBackground(bg(CARD,16)); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,58); p.bottomMargin=10; e.setLayoutParams(p); return e; }
    Button primary(String s){ Button b=new Button(this); b.setText(s); b.setTextSize(15); b.setTextColor(Color.WHITE); b.setAllCaps(false); b.setBackground(bg(BLUE,18)); b.setMinHeight(58); return b; }

    void newPolicy(){ shell("ثبت بیمه‌نامه");
        content.addView(label("اطلاعات مشتری")); EditText customer=input("نام و نام خانوادگی مشتری",InputType.TYPE_CLASS_TEXT); content.addView(customer); EditText phone=input("شماره تماس",InputType.TYPE_CLASS_PHONE); content.addView(phone);
        content.addView(label("اطلاعات خودرو")); EditText plate=input("پلاک خودرو",InputType.TYPE_CLASS_TEXT); content.addView(plate);
        content.addView(label("اطلاعات مالی")); EditText premium=input("کل حق بیمه (تومان)",InputType.TYPE_CLASS_NUMBER); content.addView(premium); EditText down=input("پیش‌پرداخت (تومان)",InputType.TYPE_CLASS_NUMBER); content.addView(down); EditText count=input("تعداد اقساط",InputType.TYPE_CLASS_NUMBER); content.addView(count); EditText day=input("روز سررسید ماه (۱ تا ۲۸)",InputType.TYPE_CLASS_NUMBER); content.addView(day);
        Button save=primary("✓  ذخیره و ساخت جدول اقساط"); content.addView(save); save.setOnClickListener(v->{try{String n=customer.getText().toString().trim(); int p=Integer.parseInt(premium.getText().toString()); int d=Integer.parseInt(down.getText().toString()); int c=Integer.parseInt(count.getText().toString()); int dy=Integer.parseInt(day.getText().toString()); if(n.isEmpty()||p<0||d<0||d>p||c<1||dy<1||dy>28)throw new Exception(); long id=db.addPolicy(n,phone.getText().toString(),plate.getText().toString(),p,d,c,dy); Toast.makeText(this,"بیمه‌نامه با موفقیت ثبت شد",Toast.LENGTH_LONG).show(); policyDetail(id); }catch(Exception x){Toast.makeText(this,"لطفاً همه فیلدها را صحیح وارد کنید",Toast.LENGTH_LONG).show();}});
    }

    void listPolicies(){ shell("بیمه‌نامه‌ها"); EditText s=input("جستجوی نام مشتری یا پلاک...",InputType.TYPE_CLASS_TEXT); content.addView(s); Button go=primary("⌕  جستجو"); content.addView(go); LinearLayout results=vbox(); content.addView(results); Runnable load=()->{results.removeAllViews(); for(Policy p:db.policies(s.getText().toString())){ TextView c=policyCard(p); results.addView(c); c.setOnClickListener(v->policyDetail(p.id)); }}; go.setOnClickListener(v->load.run()); load.run(); }

    TextView policyCard(Policy p){ String status=p.debt==0?"تسویه‌شده":"مانده: "+money(p.debt)+" تومان"; TextView t=text(p.customer+"\n"+p.plate+"   •   "+status,15,TEXT); t.setPadding(18,14,18,14); t.setBackground(bg(CARD,18)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,82); lp.bottomMargin=10; t.setLayoutParams(lp); return t; }

    void policyDetail(long id){ shell("جزئیات بیمه‌نامه"); Policy p=db.policy(id); TextView info=text("مشتری: "+p.customer+"\nخودرو: "+p.plate+"\nکل حق بیمه: "+money(p.premium)+" تومان\nپیش‌پرداخت: "+money(p.down)+" تومان\nمانده: "+money(p.debt)+" تومان",16,TEXT); info.setPadding(18,16,18,16); info.setBackground(bg(CARD,18)); content.addView(info); content.addView(space(14)); TextView h=text("جدول اقساط",18,TEXT); h.setTypeface(null,1); content.addView(h); content.addView(space(8));
        for(Install i:db.installments(id)){ TextView card=text("قسط "+i.no+"  |  "+i.due+"\n"+money(i.amount)+" تومان     "+(i.paid?"✓ پرداخت شده":"● پرداخت نشده"),15,TEXT); card.setPadding(18,14,18,14); card.setBackground(bg(CARD,18)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,74); lp.bottomMargin=8; content.addView(card,lp); if(!i.paid){ Button pay=primary("ثبت پرداخت قسط "+i.no); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,54); bp.bottomMargin=10; content.addView(pay,bp); pay.setOnClickListener(v->{db.pay(i.id);policyDetail(id);}); }}
    }

    void report(){ shell("گزارش مالی"); content.addView(reportBox("کل حق بیمه",money(db.sumPremium())+" تومان",BLUE)); content.addView(reportBox("کل پیش‌پرداخت",money(db.sumDown())+" تومان",CYAN)); content.addView(reportBox("کل دریافت",money(db.sumPaid())+" تومان",GREEN)); content.addView(reportBox("مانده مطالبات",money(db.sumDebt())+" تومان",RED)); content.addView(reportBox("اقساط معوق",String.valueOf(db.overdue()),ORANGE)); }
    TextView reportBox(String a,String b,int c){ TextView t=text(a+"\n"+b,17,TEXT); t.setPadding(20,16,20,16); t.setTypeface(null,1); t.setBackground(bg(CARD,18)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,92); lp.bottomMargin=10; t.setLayoutParams(lp); return t; }

    void installmentBoard(){ shell("سررسید اقساط"); for(InstallRow r:db.upcoming()){ TextView t=text(r.customer+"\nقسط "+r.no+" • "+r.due+" • "+money(r.amount)+" تومان",15,TEXT); t.setPadding(18,14,18,14); t.setBackground(bg(CARD,18)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,78); lp.bottomMargin=8; content.addView(t); }}
    void searchPolicies(){ listPolicies(); }

    String money(int n){ return NumberFormat.getNumberInstance(Locale.US).format(n); }

    static class Policy{long id;String customer,plate;int premium,down,debt;Policy(long i,String c,String pl,int pr,int d,int de){id=i;customer=c;plate=pl;premium=pr;down=d;debt=de;}}
    static class Install{long id;int no,amount;String due;boolean paid;Install(long i,int n,int a,String u,boolean p){id=i;no=n;amount=a;due=u;paid=p;}}
    static class InstallRow{String customer,due;int no,amount;InstallRow(String c,int n,String d,int a){customer=c;no=n;due=d;amount=a;}}

    static class DB extends SQLiteOpenHelper{
        DB(Context c){super(c,"bimeh_pro.db",null,2);}
        public void onCreate(SQLiteDatabase x){ x.execSQL("CREATE TABLE policies(id INTEGER PRIMARY KEY AUTOINCREMENT,customer TEXT,phone TEXT,plate TEXT,premium INTEGER,downpay INTEGER,count INTEGER,firstday INTEGER)"); x.execSQL("CREATE TABLE installments(id INTEGER PRIMARY KEY AUTOINCREMENT,policy INTEGER,no INTEGER,amount INTEGER,due TEXT,paid INTEGER DEFAULT 0,paydate TEXT)"); }
        public void onUpgrade(SQLiteDatabase x,int a,int b){ if(a<2)x.execSQL("ALTER TABLE installments ADD COLUMN paydate TEXT"); }
        long addPolicy(String c,String ph,String pl,int pr,int d,int cc,int day){SQLiteDatabase x=getWritableDatabase();ContentValues v=new ContentValues();v.put("customer",c);v.put("phone",ph);v.put("plate",pl);v.put("premium",pr);v.put("downpay",d);v.put("count",cc);v.put("firstday",day);long id=x.insert("policies",null,v);int rem=pr-d,q=rem/cc,r=rem%cc;Calendar cal=Calendar.getInstance(); cal.set(Calendar.DAY_OF_MONTH,1);for(int n=1;n<=cc;n++){cal.set(Calendar.DAY_OF_MONTH,Math.min(day,28));cal.add(Calendar.MONTH,1);int amt=q+(n<=r?1:0);String due=new SimpleDateFormat("yyyy/MM/dd",Locale.US).format(cal.getTime());ContentValues z=new ContentValues();z.put("policy",id);z.put("no",n);z.put("amount",amt);z.put("due",due);x.insert("installments",null,z);}return id;}
        int sum(String q){Cursor c=getReadableDatabase().rawQuery(q,null);int v=0;if(c.moveToFirst())v=c.getInt(0);c.close();return v;}
        int countPolicies(){return sum("SELECT COUNT(*) FROM policies");} int sumPremium(){return sum("SELECT COALESCE(SUM(premium),0) FROM policies");} int sumDown(){return sum("SELECT COALESCE(SUM(downpay),0) FROM policies");} int sumPaid(){return sum("SELECT COALESCE(SUM(amount),0) FROM installments WHERE paid=1");} int sumDebt(){return sumPremium()-sumDown()-sumPaid();}
        int overdue(){return sum("SELECT COUNT(*) FROM installments WHERE paid=0 AND due < strftime('%Y/%m/%d','now','localtime')");}
        ArrayList<Policy> policies(String filter){ArrayList<Policy>a=new ArrayList<>();String f="%"+(filter==null?"":filter)+"%";Cursor c=getReadableDatabase().rawQuery("SELECT id,customer,plate,premium,downpay FROM policies WHERE customer LIKE ? OR plate LIKE ? ORDER BY id DESC",new String[]{f,f});while(c.moveToNext()){long id=c.getLong(0);int pr=c.getInt(3),d=c.getInt(4),paid=paid(id);a.add(new Policy(id,c.getString(1),c.getString(2),pr,d,pr-d-paid));}c.close();return a;}
        int paid(long id){return sum("SELECT COALESCE(SUM(amount),0) FROM installments WHERE policy="+id+" AND paid=1");}
        Policy policy(long id){Cursor c=getReadableDatabase().rawQuery("SELECT customer,plate,premium,downpay FROM policies WHERE id=?",new String[]{""+id});c.moveToFirst();String n=c.getString(0),pl=c.getString(1);int pr=c.getInt(2),d=c.getInt(3);c.close();return new Policy(id,n,pl,pr,d,pr-d-paid(id));}
        ArrayList<Install> installments(long id){ArrayList<Install>a=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT id,no,amount,due,paid FROM installments WHERE policy=? ORDER BY no",new String[]{""+id});while(c.moveToNext())a.add(new Install(c.getLong(0),c.getInt(1),c.getInt(2),c.getString(3),c.getInt(4)==1));c.close();return a;}
        void pay(long id){ContentValues v=new ContentValues();v.put("paid",1);v.put("paydate",new SimpleDateFormat("yyyy/MM/dd",Locale.US).format(new Date()));getWritableDatabase().update("installments",v,"id=?",new String[]{""+id});}
        ArrayList<InstallRow> upcoming(){ArrayList<InstallRow>a=new ArrayList<>();Cursor c=getReadableDatabase().rawQuery("SELECT p.customer,i.no,i.due,i.amount FROM installments i JOIN policies p ON p.id=i.policy WHERE i.paid=0 ORDER BY i.due LIMIT 50",null);while(c.moveToNext())a.add(new InstallRow(c.getString(0),c.getInt(1),c.getString(2),c.getInt(3)));c.close();return a;}
    }
}
