package mapRunner.customer;

public class CustomerView extends javax.swing.JFrame {

	private static final long serialVersionUID = 7764945658121492598L;

    public CustomerView() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tfAgent = new javax.swing.JTextField();
        lAgent = new javax.swing.JLabel();
        lTarget = new javax.swing.JLabel();
        tfTarget = new javax.swing.JTextField();
        bRun = new javax.swing.JButton();
        lStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setLocationByPlatform(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        tfAgent.setText("runner");

        lAgent.setText("Agent");

        lTarget.setText("Target");

        tfTarget.setText("1");

        bRun.setText("run");
        bRun.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRunActionPerformed(evt);
            }
        });

        lStatus.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lStatus.setText("status");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lAgent)
                            .addComponent(lTarget))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tfTarget)
                            .addComponent(tfAgent, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(bRun)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfAgent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lAgent))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lTarget)
                    .addComponent(tfTarget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bRun, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lStatus))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void bRunActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRunActionPerformed
    	agent.sendTargetToRunner(tfAgent.getText(), tfTarget.getText());        
    }//GEN-LAST:event_bRunActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        agent.doDelete();
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bRun;
    private javax.swing.JLabel lAgent;
    private javax.swing.JLabel lStatus;
    private javax.swing.JLabel lTarget;
    private javax.swing.JTextField tfAgent;
    private javax.swing.JTextField tfTarget;
    // End of variables declaration//GEN-END:variables
    
    CustomerAgent agent;
    
    public void showStatus(String status) {
    	lStatus.setText(status);
    }
}
