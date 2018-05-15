package cy.com.android.mmitest.service;

interface INvRamService {
   byte[] readINvramInfo(int length);
   void writeToNvramInfo(in byte[] sn_buff, int length);
   int eraseSdCard();
   int getSimStatus(int id);
  }