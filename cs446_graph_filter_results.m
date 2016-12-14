%initialization of all matrix data
acl1f = 'acl1_seed_1000.filter.results.csv';
acl3f =  'acl3_seed_1000.filter.results.csv';
acl4f = 'acl4_seed_1000.filter.results.csv';
acl5f ='acl5_seed_1000.filter.results.csv';
fw1f  ='fw1_seed_1000.filter.results.csv';
fw2f  ='fw2_seed_1000.filter.results.csv';
fw3f  ='fw3_seed_1000.filter.results.csv';
fw4f  ='fw4_seed_1000.filter.results.csv';
fw5f  ='fw5_seed_1000.filter.results.csv';
ipc1f ='ipc1_seed_1000.filter.results.csv';
ipc2f ='ipc2_seed_1000.filter.results.csv';
fw1kf  ='fw1_seed_5000.filter.results.csv';
%fw1kf  ='fw1_seed_5000.filter.results.csv';
acl1mat = csvread(acl1f);
acl3mat = csvread(acl3f);
acl4mat = csvread(acl4f);
acl5mat = csvread(acl5f);
fw1mat = csvread(fw1f);
fw2mat = csvread(fw2f);
fw3mat = csvread(fw3f);
fw4mat = csvread(fw4f);
fw5mat = csvread(fw5f);
ipc1mat = csvread(ipc1f);
ipc2mat = csvread(ipc2f);
fw1kmat = csvread(fw1kf);

avg_acl = (acl1mat(:,1:900) + acl3mat(:,1:900) + acl4mat(:,1:900) + acl5mat(:,1:900))/4;
avg_fw = (fw1mat(:,1:800) + fw2mat(:,1:800) + fw3mat(:,1:800) + fw4mat(:,1:800) + fw5mat(:,1:800))/5;
total_avg = (avg_acl(:,1:800)*4 + avg_fw*5 + ipc1mat(:,1:800))/10;

m = fw1mat;%this is the matrix that actually gets graphed
 
%format long;
%formatSpec = '%f';sizeA = [Inf 5];fileID = fopen(fn,'r');m = fscanf(fileID,formatSpec,sizeA);fclose(fileID);
%%disp(m');
x = m(1,:);
Errs = m(2,:);
cost = m(4,:);
dE = m(5,:);
logdE = m(6,:);

subplot(2,2,1);
plot(x,Errs);
title('total Error vs number of clusters');

subplot(2,2,2);
plot(x,dE);
title('dError vs number of clusters');

subplot(2,2,3);
plot(x,logdE)
title('log10 of dE');

subplot(2,2,4);
plot(x,cost);
title('cost=(number of clusters)*(Error)');

%title(fn)
%xlabel('number of clusters')
%ylabel('cos(5x)')