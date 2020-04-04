package erp.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 当用户推出系统时, 自动备份所有财务数据
 */
@WebListener
public class BackupsListener implements HttpSessionListener {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    // 数据库帐号
    @Value("${spring.datasource.druid.username}")
    private String username;
    // 数据库密码
    @Value("${spring.datasource.druid.password}")
    private String password;
    // 需要备份的数据库名
    @Value("${datasource.database}")
    private String database;

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        backups();
    }

    private void backups() {
        // 数据库导出
        InputStream in = null;
        FileOutputStream out = null;
        try {
            //cmd命令拼接
            String cmd = "mysqldump -u" + username + " -p" + password + " " + database;
            //执行命令
            Process process = Runtime.getRuntime().exec(cmd);
            // 生成的文件名
            String fileName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            // 需要备份到的路径
            String filepath = "file:D:\\cai_wu_shu_jv\\" + fileName + ".sql";
            //如果地址有空格，为解决找不到路径所以用了这个方式
            URL url = new URL(filepath);
            String path = url.getPath();
            //自动创建目录
            File file = new File(path);
            if (!file.getParentFile().exists()) {
                if (file.getParentFile().mkdirs()) {
                    throw new RuntimeException("创建备份目录失败");
                }
            }

            in = process.getInputStream();
            out = new FileOutputStream(file);
            byte[] data = new byte[1024 * 4];
            int len;
            while ((len = in.read(data)) != -1) {
                out.write(data, 0, len);
            }
            System.out.println("数据已备份到文件 " + filepath);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
