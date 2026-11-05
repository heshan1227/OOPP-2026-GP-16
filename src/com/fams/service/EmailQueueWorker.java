package com.fams.service;

import com.fams.database.DBConnection;
import java.sql.*;

/**
 * Background email dispatcher. HTTP/Swing actions only enqueue email_queue rows;
 * an SMTP-backed MailSender can be supplied by deployment configuration.
 */
public final class EmailQueueWorker implements AutoCloseable {
    @FunctionalInterface public interface MailSender {
        void send(String recipient,String subject,String body) throws Exception;
    }
    private volatile boolean running;
    private Thread worker;
    public synchronized void start(MailSender sender) {
        if(running) return;
        if(sender==null) throw new IllegalArgumentException("MailSender is required.");
        running=true;
        worker=new Thread(()->runLoop(sender),"fams-email-queue");
        worker.setDaemon(true);
        worker.start();
    }
    private void runLoop(MailSender sender) {
        while(running) {
            try {
                Email email=claim();
                if(email==null) { Thread.sleep(1000); continue; }
                try {
                    sender.send(email.to,email.subject,email.body);
                    update(email.id,"SENT",true);
                } catch(Exception ex) { update(email.id,"FAILED",false); }
            } catch(InterruptedException ex) { Thread.currentThread().interrupt(); return; }
            catch(Exception ex) {
                try { Thread.sleep(2000); } catch(InterruptedException stop) { Thread.currentThread().interrupt(); return; }
            }
        }
    }
    private Email claim() throws SQLException {
        try(Connection c=DBConnection.getConnection()) {
            if(c==null)return null;
            c.setAutoCommit(false);
            try(PreparedStatement p=c.prepareStatement(
                "SELECT email_id,recipient_email,subject,body FROM email_queue WHERE status='PENDING' ORDER BY email_id LIMIT 1 FOR UPDATE SKIP LOCKED");
                ResultSet r=p.executeQuery()) {
                if(!r.next()){c.commit();return null;}
                Email e=new Email(r.getLong(1),r.getString(2),r.getString(3),r.getString(4));
                try(PreparedStatement u=c.prepareStatement("UPDATE email_queue SET status='SENDING',attempts=attempts+1 WHERE email_id=?")){u.setLong(1,e.id);u.executeUpdate();}
                c.commit();return e;
            } catch(Exception ex){c.rollback();if(ex instanceof SQLException se)throw se;throw new SQLException(ex);}
            finally{c.setAutoCommit(true);}
        }
    }
    private void update(long id,String status,boolean sent) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(
            "UPDATE email_queue SET status=?,sent_at=? WHERE email_id=?")) {
            p.setString(1,status);if(sent)p.setTimestamp(2,new Timestamp(System.currentTimeMillis()));else p.setNull(2,Types.TIMESTAMP);
            p.setLong(3,id);p.executeUpdate();
        }
    }
    @Override public synchronized void close() {
        running=false;
        if(worker!=null)worker.interrupt();
    }
    private record Email(long id,String to,String subject,String body) {}
}
