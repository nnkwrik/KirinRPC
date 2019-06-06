package benchmark.serialication;

import com.google.common.collect.Lists;
import io.github.nnkwrik.kirinrpc.serializer.Serializer;
import io.github.nnkwrik.kirinrpc.serializer.SerializerHolder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.util.internal.PlatformDependent;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author nnkwrik
 * @date 19/06/05 8:53
 */
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SerializationBenchmark {
    /*
        Benchmark                                     Mode  Cnt    Score     Error   Units
        SerializationBenchmark.protoStuffBytesArray  thrpt   10  857.985 ± 105.568  ops/ms
        SerializationBenchmark.protoStuffBytesArray   avgt   10    0.001 ±   0.001   ms/op
     */
    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(SerializationBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }

    private static final Serializer protoStuffSerializer = SerializerHolder.serializerImpl();

    private static final ByteBufAllocator allocator = new PooledByteBufAllocator(PlatformDependent.directBufferPreferred());

    static int USER_COUNT = 1;

    @Benchmark
    public void protoStuffBytesArray() {
        // 写入
        byte[] bytes = protoStuffSerializer.writeObject(createUsers(USER_COUNT));
        ByteBuf byteBuf = allocator.buffer(bytes.length);
        byteBuf.writeBytes(bytes);

        // 网络传输都是相同的条件

        // 读出
        bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        protoStuffSerializer.readObject(bytes, Users.class);

        // 释放
        byteBuf.release();
    }

    static Users createUsers(int count) {
        List<User> userList = Lists.newArrayListWithCapacity(count);
        for (int i = 0; i < count; i++) {
            userList.add(createUser());
        }
        Users users = new Users();
        users.setUsers(userList);
        return users;
    }

    static User createUser() {
        User user = new User();
        user.setId(1L);
        user.setName("block");
        user.setSex(0);
        user.setBirthday(new Date());
        user.setEmail("xxx@alibaba-inc.con");
        user.setMobile("18325038521");
        user.setAddress("浙江省 杭州市 文一西路969号");
        List<Integer> permsList = Lists.newArrayList(
                1, 12, 123
//                , Integer.MAX_VALUE, Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 2
//                , Integer.MIN_VALUE, Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2
        );
        user.setPermissions(permsList);
        user.setStatus(1);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        return user;
    }

    static class Users implements Serializable {

        private List<User> users;

        public List<User> getUsers() {
            return users;
        }

        public void setUsers(List<User> users) {
            this.users = users;
        }
    }

    static class User implements Serializable {

        private long id;
        private String name;
        private int sex;
        private Date birthday;
        private String email;
        private String mobile;
        private String address;
        private List<Integer> permissions;
        private int status;
        private Date createTime;
        private Date updateTime;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getSex() {
            return sex;
        }

        public void setSex(int sex) {
            this.sex = sex;
        }

        public Date getBirthday() {
            return birthday;
        }

        public void setBirthday(Date birthday) {
            this.birthday = birthday;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public List<Integer> getPermissions() {
            return permissions;
        }

        public void setPermissions(List<Integer> permissions) {
            this.permissions = permissions;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public Date getCreateTime() {
            return createTime;
        }

        public void setCreateTime(Date createTime) {
            this.createTime = createTime;
        }

        public Date getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Date updateTime) {
            this.updateTime = updateTime;
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", sex=" + sex +
                    ", birthday=" + birthday +
                    ", email='" + email + '\'' +
                    ", mobile='" + mobile + '\'' +
                    ", address='" + address + '\'' +
                    ", permissions=" + permissions +
                    ", status=" + status +
                    ", createTime=" + createTime +
                    ", updateTime=" + updateTime +
                    '}';
        }
    }
}
